package com.security.demo.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.security.demo.dto.ExcelDataDto;
import lombok.extern.log4j.Log4j2;
import reactor.util.function.Tuple2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public abstract class ExportExcelUtils {

    private static final String DEFAULT_BASE_PATH = System.getProperty("user.dir");

    public static <T extends BaseRowModel> void   exportExcel(Tuple2<String,List<T>> tuple2,Class<T> clazz) {
        Path path = Paths.get(DEFAULT_BASE_PATH, "data");
        Optional.of(path)
                .filter(p-> Files.notExists(p))
                .ifPresent(s-> {
                    try {
                        Files.createDirectories(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        log.info("################# : " + tuple2.getT2().size());
        String filePath = path.toUri().getPath() + String.format("%s",tuple2.getT1());
        log.info(filePath.substring(1));
        try(OutputStream out = new FileOutputStream(filePath.substring(1))){
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
            Sheet sheet1 = new Sheet(1, 1, clazz);
            writer.write(tuple2.getT2(), sheet1);
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
