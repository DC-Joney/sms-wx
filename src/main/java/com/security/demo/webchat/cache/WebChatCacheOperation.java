package com.security.demo.webchat.cache;

import lombok.Builder;
import lombok.Data;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Builder
@Data
public class WebChatCacheOperation {

   private CacheManager cacheManager;

   private CacheErrorHandler cacheErrorHandler;

}
