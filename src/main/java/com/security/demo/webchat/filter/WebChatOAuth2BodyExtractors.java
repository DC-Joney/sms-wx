package com.security.demo.webchat.filter;

import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import reactor.core.publisher.Mono;

class WebChatOAuth2BodyExtractors {

    public static BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> oauth2AccessTokenResponse() {
        return new WebChatAccessTokenResponseBodyExtractor();
    }

    private WebChatOAuth2BodyExtractors() {}
}
