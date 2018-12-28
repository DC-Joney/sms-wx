package com.security.demo.webchat.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Getter
@ToString
public class WebChatCache {

    private Object value;

    @Setter
    private Instant expireTime;

    public WebChatCache(Object value, Instant refreshTime){

        this.value = Objects.requireNonNull(value);

        this.expireTime = refreshTime.minus(Duration.ofSeconds(10));

    }

    public <T> T getValue() {
        return (T) value;
    }
}
