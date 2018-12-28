package com.security.demo.webchat.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;


public interface WebChatCacheOperation {

    CacheManager cacheManager();

    CacheErrorHandler cacheErrorHandler();

}
