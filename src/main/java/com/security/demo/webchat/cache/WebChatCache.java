package com.security.demo.webchat.cache;

import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Getter
@ToString
public class WebChatCache {

    private Object value;

    private Instant expireTime;

    public WebChatCache(Object value, long refreshTime){

        this.value = Objects.requireNonNull(value);

        this.expireTime = Instant.now().plus(Duration.ofSeconds(refreshTime - 100));

    }

    public <T> T getValue() {
        return (T) value;
    }
}
