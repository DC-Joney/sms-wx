package com.security.demo.secuirty.config;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Component
public class WebfluxConfiguration implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/search/**")
                .allowCredentials(true)
                .allowedMethods("*")
                .allowedOrigins("*");

    }


}
