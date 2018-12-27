package com.security.demo.web;

import com.security.demo.dto.ExcelDataDto;
import com.security.demo.excel.ExcelUtil;
import com.security.demo.excel.ExportExcelUtils;
import com.security.demo.test.SearchProvinceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RestController
public class DemoController {

    @GetMapping("/search/area")
    public Mono<String> searchProvince(@RequestParam("province") String province,
                                       @RequestParam("village") String village) {
        return SearchProvinceUtils.searchArea(province, village);
    }

    @PostMapping("/search/upload")
    public Mono<Void> uploadExcel(@RequestPart("file") FilePart filePart) {
        return Mono.using(() -> Files.createTempFile(Paths.get(System.getProperty("user.dir"), "files"), null, filePart.filename()),
                temp -> filePart.transferTo(temp)
                                .thenMany(Flux.fromIterable(ExcelUtil.readExcel(temp, ExcelDataDto.class,1)))
                                .bufferTimeout(100, Duration.ofSeconds(2))
                                .delayElements(Duration.ofSeconds(2))
                                .doOnNext(list-> log.info(list.size()))
                                .flatMap(this::handleDto)
                                .reduceWith(ReactiveArrayList<ExcelDataDto>::new, ReactiveArrayList::addAll)
                                .map(oldList -> Tuples.of(filePart.filename(),oldList.toList()))
                                .doOnNext(tuple2 -> ExportExcelUtils.exportExcel(computedList(tuple2),ExcelDataDto.class))
                                .then(),
                this::deleteTemplateFile);
    }

    private <E extends ExcelDataDto> Tuple2<String,List<E>>  computedList(Tuple2<String,List<E>> oldTuple2){

        List<E> newList = oldTuple2.getT2()
                .stream()
                .sorted(Comparator.comparing(ExcelDataDto::getId))
                .collect(Collectors.toList());

        return Tuples.of(oldTuple2.getT1(),newList);
    }

    private void deleteTemplateFile(Path tempPath){
            try {
                Files.deleteIfExists(tempPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private Mono<String> findArea(ExcelDataDto excelDataDto){
        return Mono.fromCompletionStage(SearchProvinceUtils.searchArea(excelDataDto.getCityName(),
                 computedVillage(excelDataDto.getCountySeat() + excelDataDto.getVillage())).toFuture());
    }


    private String computedVillage(String village){
        return Optional.ofNullable(village)
                .filter(s-> !s.endsWith("村"))
                .map(s-> s + "村")
                .orElse(village);
    }

    private Mono<ReactiveArrayList<ExcelDataDto>> handleDto(List<ExcelDataDto> excelDataDtos){
        return  Flux.fromIterable(excelDataDtos)
                .flatMap(excelDataDto->
                        Mono.fromSupplier(()-> excelDataDto)
                        .filter(dto-> StringUtils.hasText(dto.getCityName()) && dto.getTown() == null && StringUtils.hasText(dto.getVillage()))
                        .zipWith(findArea(excelDataDto),ExcelDataDto::setTown)
                        .thenReturn(excelDataDto))
                .reduceWith(ReactiveArrayList::new, ReactiveArrayList::add);
    }


    @GetMapping("/search/download")
    public Mono<Void> downloadByWriteWith(ServerHttpResponse response) throws IOException {
        ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=parallel.png");
        response.getHeaders().setContentType(MediaType.IMAGE_PNG);
        Resource resource = new ClassPathResource("parallel.png");
        File file = resource.getFile();
        return zeroCopyResponse.writeWith(file, 0, file.length());
    }



    @GetMapping("/search/test")
    public Mono<String> test(){
        return Mono.just("111");
    }
}

//                                {
//
//
//
//
//                                    if(StringUtils.hasText(excelDataDto.getCityName()) && excelDataDto.getTown() == null){
//                                        return SearchProvinceUtils.searchArea(excelDataDto.getCityName(), excelDataDto.getCountySeat() + excelDataDto.getVillage())
//                                                .map(excelDataDto::setTown);
//                                    }
//                                    return Mono.just(excelDataDto);
//                                })
