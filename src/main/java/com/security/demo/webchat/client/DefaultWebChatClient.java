package com.security.demo.webchat.client;

import com.security.demo.webchat.WebChatDto;
import com.security.demo.webchat.cache.DefaultWebChatCachingSupport;
import com.security.demo.webchat.cache.WebChatCache;
import com.security.demo.webchat.cache.WebChatCacheOperation;
import com.security.demo.webchat.digest.WebChatDigestSign;
import com.security.demo.webchat.digest.WebChatNonceString;
import com.security.demo.webchat.digest.WebChatTimestamp;
import com.security.demo.webchat.exception.PageUrlEmpty;
import com.security.demo.webchat.exception.TicketNotFoundException;
import com.security.demo.webchat.properties.WebChatProperties;
import com.security.demo.webchat.support.WebChatParameterNames;
import com.security.demo.webchat.utils.JsonTokens;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Log4j2
public class DefaultWebChatClient implements WebChatClient {

    private WebClient webClient;

    private WebChatNonceString nonceString;

    private WebChatTimestamp timestamp;

    private WebChatProperties properties;

    private String appId;

    private WebChatCacheOperation cacheOperation;

    DefaultWebChatClient(WebChatProperties properties, WebClient webClient,
                         WebChatNonceString nonceString, WebChatTimestamp timestamp) {
//        Hooks.onOperatorDebug();
        this.webClient = webClient;
        this.nonceString = nonceString;
        this.timestamp = timestamp;
        this.properties = properties;
        this.appId = properties.getClientId();
        this.cacheOperation = DefaultWebChatCachingSupport.getSharedInstance().getCacheOperation();
    }


    @Override
    public WebChatClient cacheOperation(WebChatCacheOperation operation) {
        this.cacheOperation = operation;
        return this;
    }

    @Override
    public WebChatClient cacheManager(CacheManager cacheManager) {
        this.cacheOperation.setCacheManager(cacheManager);
        return this;
    }

    @Override
    public WebChatClient appId(String appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public Mono<WebChatDto> toWebChatDto(String pageUrl) {
        Cache cache = getCache(WebChatParameterNames.WEB_CHAT_VALUE_CACHE);
        return Mono.justOrEmpty(cache.get(pageUrl, WebChatCache.class))
                .filter(webChatCache -> Objects.nonNull(webChatCache) && webChatCache.getExpireTime().isAfter(Instant.now()))
                .switchIfEmpty(fromCacheTicket(pageUrl))
                .switchIfEmpty(Mono.defer(() -> trendsDto(pageUrl)))
                .map(WebChatCache::getValue)
                .cast(WebChatDto.class);
    }


    private Mono<WebChatCache> fromCacheTicket(String pageUrl) {
        Cache cache = getCache(WebChatParameterNames.TICKET_VALUE_CACHE);
        return Mono.defer(() ->
                Mono.justOrEmpty(cache.get(WebChatParameterNames.TICKET_JSON_NAME, WebChatCache.class))
                        .filter(webChatCache -> Objects.nonNull(webChatCache) && webChatCache.getExpireTime().isAfter(Instant.now()))
                        .flatMap(webChatCache ->
                                webChatSign(Mono.fromCallable(() ->
                                                buildContext(webChatCache.getValue(), webChatCache.getExpireTime().getLong(ChronoField.INSTANT_SECONDS)))
                                        , pageUrl))
        );
    }


    private Mono<WebChatCache> trendsDto(String pageUrl) {
        log.info("The ticket is not hit the cache");
        return webClient.get()
                .uri(properties.getTicketUrl())
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        throw WebClientResponseException.create(response.rawStatusCode(),
                                "Cannot get token, expected 2xx HTTP Status code",
                                null,
                                null,
                                null
                        );
                    }
                    return webChatSign(response.body(bodyExtractor()), pageUrl);
                });
    }


    private Mono<WebChatCache> webChatSign(Mono<Context> publisher, String pageUrl) {

        Cache cache = getCache(WebChatParameterNames.WEB_CHAT_VALUE_CACHE);

        Mono<String> pageUrlMono = Mono.justOrEmpty(pageUrl)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new PageUrlEmpty("The page url not empty"))));

        Mono<Context> contextMono = Mono.from(publisher)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TicketNotFoundException("The request of ticket is fail"))))
                .cache();

        return Mono.zip(pageUrlMono, contextMono)
                .flatMap(tuple2 -> WebChatDigestSign.digestSign(buildRequest(tuple2)))
                .zipWith(contextMono, (webChatDto, context) -> {
                    WebChatCache webChatCache = new WebChatCache(webChatDto, context.<Long>get(WebChatParameterNames.EXPIRES_IN));
                    cache.put(pageUrl, webChatCache);
                    return webChatCache;
                });
    }


    private WebChatRequest buildRequest(Tuple2<String, Context> tuple2) {
        return WebChatRequest
                .builder()
                .nonceStr(nonceString.nonceStr())
                .timestamp(timestamp.timestamp().getLong(ChronoField.INSTANT_SECONDS))
                .url(tuple2.getT1())
                .jsTicket(tuple2.getT2().get(WebChatParameterNames.TICKET_JSON_NAME))
                .appId(appId).build();
    }


    //   JSONObjectUtils
    private BodyExtractor<Mono<Context>, ReactiveHttpInputMessage> bodyExtractor() {

        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {};
        BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors.toMono(type);
        Cache cache = getCache(WebChatParameterNames.TICKET_VALUE_CACHE);

        return (inputMessage, context) ->
                delegate.extract(inputMessage, context)
                        .map(json -> {
                            JSONObject jsonObject = new JSONObject(json);
                            String ticket = JsonTokens.parse(jsonObject, WebChatParameterNames.TICKET_JSON_NAME);
                            long expire = JsonTokens.expire(jsonObject, WebChatParameterNames.EXPIRES_IN);
                            WebChatCache webChatCache = new WebChatCache(ticket, expire);
                            cache.put(WebChatParameterNames.TICKET_JSON_NAME, webChatCache);
                            log.info("ticket : " + ticket);
                            return buildContext(ticket, expire);
                        });
    }


    private Context buildContext(String ticket, long expire) {
        return Context.of(WebChatParameterNames.TICKET_JSON_NAME, ticket, WebChatParameterNames.EXPIRES_IN, expire);
    }


    private Cache getCache(String cacheName) {
        return Optional.ofNullable(cacheName)
                .map(name -> cacheOperation.getCacheManager().getCache(name))
                .orElseThrow(() -> new RuntimeException("The CacheManager cache must not be null"));
    }


    @Data
    @lombok.Builder
    public static class WebChatRequest {
        private String appId;
        private String jsTicket;
        private String nonceStr;
        private long timestamp;
        private String url;
    }
}
