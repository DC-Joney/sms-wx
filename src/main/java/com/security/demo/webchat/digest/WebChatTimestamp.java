package com.security.demo.webchat.digest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@FunctionalInterface
public interface WebChatTimestamp {

    Instant timestamp();

    static WebChatTimestamp defaultTimestamp(){
        return () -> Instant.now().plus(1000, ChronoUnit.SECONDS);
    }

}
