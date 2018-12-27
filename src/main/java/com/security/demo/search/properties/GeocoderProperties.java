package com.security.demo.search.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.reactive.geocoder")
public class GeocoderProperties {

    private String key;



}
