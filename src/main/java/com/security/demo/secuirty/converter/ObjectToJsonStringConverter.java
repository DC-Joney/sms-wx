package com.security.demo.secuirty.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;

public class ObjectToJsonStringConverter implements Converter<Object,String> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
