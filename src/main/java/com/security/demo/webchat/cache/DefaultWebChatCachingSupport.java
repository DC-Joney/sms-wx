package com.security.demo.webchat.cache;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.lang.Nullable;

public class DefaultWebChatCachingSupport implements WebChatCachingConfigurer, InitializingBean {

    private WebChatCacheOperation defaultCacheOperation;

    @Nullable
    private static volatile DefaultWebChatCachingSupport cachingConfigurer;

    public static WebChatCachingConfigurer getSharedInstance() {
        DefaultWebChatCachingSupport cs = cachingConfigurer;
        if (cs == null) {
            synchronized (DefaultWebChatCachingSupport.class) {
                cs = cachingConfigurer;
                if (cs == null) {
                    cs = new DefaultWebChatCachingSupport();
                    cachingConfigurer = cs;
                }
            }
        }
        return cs;
    }

    private DefaultWebChatCachingSupport() {
        afterPropertiesSet();
    }

    private CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    private CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

    @Override
    public void afterPropertiesSet() {
        this.defaultCacheOperation = WebChatCacheOperation.builder()
                .cacheErrorHandler(errorHandler())
                .cacheManager(cacheManager())
                .build();
    }

    public WebChatCacheOperation getCacheOperation() {
        return defaultCacheOperation;
    }

}
