package com.security.demo.webchat.digest;

import com.security.demo.webchat.WebChatDto;
import com.security.demo.webchat.client.DefaultWebChatClient.WebChatRequest;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Log4j2
public class WebChatDigestSign {

    public static Mono<WebChatDto> digestSign(WebChatRequest request) {
        return Mono.justOrEmpty(request)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NullPointerException("The web_chat request must not be null!!"))))
                .transform(requestMono -> requestMono
                        .map(WebChatDigestSign::signStr)
                        .map(WebChatDigestSign::digestStr)
                )
                .zipWith(Mono.just(request), WebChatDigestSign::webChatDto);

    }

    private static WebChatDto webChatDto(String signStr, WebChatRequest request) {
        return WebChatDto.builder()
                .signature(signStr)
                .noncestr(request.getNonceStr())
                .timeStamp(request.getTimestamp())
                .appId(request.getAppId())
                .build();

    }

    private static String digestStr(String signStr) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(signStr.getBytes(StandardCharsets.UTF_8));
            return byteToHexMono(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private static String signStr(WebChatRequest request) {
        return "jsapi_ticket=" + request.getJsTicket() + "&noncestr=" + request.getNonceStr() + "&timestamp="
                + request.getTimestamp() + "&url=" + request.getUrl();
    }

    private static String byteToHexMono(final byte[] hash) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }

//        return Mono.using(Formatter::new,
//                formatter -> {
//                    for (byte b : hash) {
//                        formatter.format("%02x", b);
//                    }
//                    return Mono.justOrEmpty(formatter.toString());
//                }, Formatter::close);
    }

}
