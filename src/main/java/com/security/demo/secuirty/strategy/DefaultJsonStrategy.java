package com.security.demo.secuirty.strategy;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Log4j2
public class DefaultJsonStrategy implements JsonStrategy{

    private static final String DEFAULT_JSON_STRING = "Warring : The request emerging questions !!!!";

    @Override
    public Mono<Void> writeResponse(ServerWebExchange exchange,String value) {
        ServerHttpResponse result = exchange.getResponse();
        result.setStatusCode(HttpStatus.OK);
        result.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        return result.writeWith(createBuffer(exchange, value));
    }

    private Mono<DataBuffer> createBuffer(ServerWebExchange exchange,String jsonStr) {
        return Mono.justOrEmpty(jsonStr)
                .switchIfEmpty(getDefaultJsonString())
                .map(String::getBytes)
                .map(bytes -> {
                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    return bufferFactory.wrap(bytes);
                });
    }


    private static Mono<String> getDefaultJsonString() {
        log.warn("The request is emerging questions");
        return Mono.just(DEFAULT_JSON_STRING);
    }
}
