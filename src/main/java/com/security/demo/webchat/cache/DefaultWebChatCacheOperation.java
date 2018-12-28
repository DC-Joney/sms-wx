package com.security.demo.webchat.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;

import java.util.Optional;

public class DefaultWebChatCacheOperation implements WebChatCacheOperation{

    private final CacheManager defaultCacheManager = new ConcurrentMapCacheManager();

    private final CacheErrorHandler defaultCacheErrorHandler = new SimpleCacheErrorHandler();

    private CacheManager cacheManager;

    private CacheErrorHandler cacheErrorHandler;

    public DefaultWebChatCacheOperation() { }

    public DefaultWebChatCacheOperation(CacheManager defaultCacheManager, CacheErrorHandler defaultCacheErrorHandler) {
        this.cacheManager = defaultCacheManager;
        this.cacheErrorHandler = defaultCacheErrorHandler;
    }

    @Override
    public CacheManager cacheManager() {
        return Optional.ofNullable(cacheManager)
                .orElse(defaultCacheManager);
    }

    @Override
    public CacheErrorHandler cacheErrorHandler() {
        return Optional.ofNullable(cacheErrorHandler)
                .orElse(defaultCacheErrorHandler);
    }




}
