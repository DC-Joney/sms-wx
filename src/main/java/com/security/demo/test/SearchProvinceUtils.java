package com.security.demo.test;

import com.security.demo.secuirty.utils.UnicodeToStringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@Log4j2
public class SearchProvinceUtils {

    private static final WebClient webClient = WebClient.create("https://restapi.amap.com");


    private static final String DEFAULT_KEY = "861aea8a0b49ef222a96c95374def777";

    private static Mono<String> httpClient(Supplier<String> findUrl, String typeValue) {
        return webClient
                .get()
                .uri(findUrl.get())
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("authority", "lbs.amap.com")
                .header("ContentType", "application/x-www-form-urlencoded; charset=UTF-8")
//                .header("referer", "https://lbs.amap.com/api/webservice/guide/api/georegeo")
//                .header("x-requested-with", "XMLHttpRequest")
//                .body(BodyInserters.fromFormData("type", typeValue).with("version", "v3"))
                .acceptCharset(Charset.forName("UTF-8"))
                .cookie("guid", "a429-e056-19ab-ff6e")
                .cookie("AMAPID", "4dd94f09095344a1b7ed5c54c166e1ed")
                .cookie("UM_distinctid", "167c422fa5ca5-08c0c5eac79e72-3a3a5c0e-15f900-167c422fa5e10")
                .cookie("CNZZDATA1255621432", "649465038-1545182679-https%253A%252F%252Fwww.google.com.hk%252F%7C1545182679")
                .cookie("key", "608d75903d29ad471362f8c58c550daf")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(log::info);
    }

    private static Mono<String> matchSomeCondition(Matcher matcher){
        return Mono.just(matcher)
                .filter(Matcher::find)
                .map(matcher1 -> matcher1.group(1))
                .filter(StringUtils::hasText)
                .switchIfEmpty(Mono.error(new RuntimeException("")));
    }

    public static Mono<String> searchArea(String province,String village){
//        return Mono.fromDirect(httpClient(() -> String.format("/dev/api?address=%s&city=%s",village,province),
        return Mono.fromDirect(httpClient(() -> String.format("/v3/geocode/geo?key=%s&address=%s&city=%s",DEFAULT_KEY,village,province),
                "geocode/geo"))
                .flatMap(info-> {
                    return matchSomeCondition(Pattern.compile("\"level\":\"(.+)\"").matcher(info))
                            .map(UnicodeToStringUtils::unicodeToString)
                            .filter(address-> !address.equals("区县"))
                            .switchIfEmpty(Mono.error(new RuntimeException("")))
                            .thenReturn(Pattern.compile("\"location\":\"(.+)\",\"level").matcher(info));
                })
                .flatMap(SearchProvinceUtils::matchSomeCondition)
//                .flatMap(location-> httpClient(() -> String.format("/dev/api?location=%s&radius=0&extensions=base&batch=false&roadlevel=0",location)
                .flatMap(location-> httpClient(() -> String.format("/v3/geocode/regeo?key=%s&location=%s&radius=0&extensions=base&batch=false&roadlevel=0",DEFAULT_KEY,location)
                        ,"geocode/regeo"))
                .map(info -> Pattern.compile("\"township\":\"(.+)\",\"businessAreas").matcher(info))
                .flatMap(SearchProvinceUtils::matchSomeCondition)
                .map(UnicodeToStringUtils::unicodeToString)
                .doOnNext(log::info)
                .onErrorResume(e-> Mono.just(e.getMessage()));
    }





//    http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location=35.658651,139.745415&output=json&pois=1&ak=您的ak //GET请求
    public static void main(String[] args) {
        searchArea("石家庄","薛家庄村").blockOptional().ifPresent(log::info);
    }

//38.19155774704792,114.02510426595265
//    public static void main(String[] args) {
//
//        String location = httpClient(() -> "/dev/api?address=驸马寨&city=秦皇岛","geocode/geo")
//                .flatMap(info -> {
//                    Matcher matcher = Pattern.compile("\"location\":\"(.+)\",\"level").matcher(info);
//                    return Optional.of(matcher).filter(Matcher::find).map(matcher1 -> matcher1.group(1));
//                })
//                .orElseThrow(() -> new RuntimeException("无法查询到任何信息"));
//
//        System.out.println(location);
//
//        httpClient(() -> "/dev/api?location="+ location +"&radius=0&extensions=base&batch=false&roadlevel=0","geocode/regeo")
//                .flatMap(info -> {
//                    Matcher matcher = Pattern.compile("\"township\":\"(.+)\",\"businessAreas").matcher(info);
//                    return Optional.of(matcher).filter(Matcher::find).map(matcher1 -> matcher1.group(1));
//                })
//                .ifPresent(info-> log.info(UnicodeToStringUtils.unicodeToString(info)));
//
////        String block = WebClient.create("https://lbs.amap.com")
////                .post()
////                .uri("/dev/api?address=驸马寨&city=秦皇岛")
////                .header("Accept","application/json, text/javascript, */*; q=0.01")
////                .header("authority","lbs.amap.com")
////                .header("ContentType","application/x-www-form-urlencoded; charset=UTF-8")
////                .header("referer","https://lbs.amap.com/api/webservice/guide/api/georegeo")
////                .header("x-requested-with","XMLHttpRequest")
////                .body(BodyInserters.fromFormData("type", "geocode/geo").with("version", "v3"))
////                .acceptCharset(Charset.forName("UTF-8"))
////                .cookie("guid", "a429-e056-19ab-ff6e")
////                .cookie("AMAPID", "4dd94f09095344a1b7ed5c54c166e1ed")
////                .cookie("UM_distinctid", "167c422fa5ca5-08c0c5eac79e72-3a3a5c0e-15f900-167c422fa5e10")
////                .cookie("CNZZDATA1255621432", "649465038-1545182679-https%253A%252F%252Fwww.google.com.hk%252F%7C1545182679")
////                .cookie("key", "608d75903d29ad471362f8c58c550daf")
////                .retrieve()
////                .bodyToMono(String.class).block();
////        Matcher matcher = Pattern.compile("\"location\":\"(.+)\",\"level").matcher(block);
////        if (matcher.find( )) {
////            System.out.println("Found value: " + matcher.group(1) );
////        } else {
////            System.out.println("NO MATCH");
////        }
////        System.out.println(block);
//    }








}
