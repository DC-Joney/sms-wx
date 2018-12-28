package com.security.demo.webchat.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.function.Consumer;

public interface WebChatCachingConfigurer extends InitializingBean {

    WebChatCacheOperation getCacheOperation();

    default void configurer(Consumer<WebChatCacheOperation> cacheOperation){
        cacheOperation.accept(getCacheOperation());
    }


}
