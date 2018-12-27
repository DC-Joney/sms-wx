package com.security.demo.secuirty.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class JsonConvertUtils {

    private static ConversionService conversionService;

    static {
        conversionService = DefaultConversionService.getSharedInstance();
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static <T> CompletionStage<String> convertToString(T obj) {

      Assert.notNull(obj,"Convert obj must not be null");

      return CompletableFuture.supplyAsync(()-> conversionService.convert(obj,String.class));
    }

}
