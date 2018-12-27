package com.security.demo.webchat.client;

import com.security.demo.webchat.digest.WebChatNonceString;
import com.security.demo.webchat.digest.WebChatTimestamp;
import com.security.demo.webchat.properties.WebChatProperties;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.Optional;

final class DefaultWebChatClientBuilder implements WebChatClient.Builder {

    private  WebChatProperties  properties;

    private  WebClient webClient;

    private WebChatNonceString defaultNonceString = WebChatNonceString.defaultNonceStr();

    private WebChatNonceString nonceString;

    private WebChatTimestamp defaultTimestamp = WebChatTimestamp.defaultTimestamp();

    private WebChatTimestamp timestamp;


    DefaultWebChatClientBuilder(WebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient);
    }


    @Override
    public WebChatClient.Builder nonceString(WebChatNonceString nonceString) {
        this.nonceString = nonceString;
        return this;
    }

    @Override
    public WebChatClient.Builder timestamp(WebChatTimestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public WebChatClient build() {
        return new DefaultWebChatClient(properties,webClient, getNonceString(), getTimestamp());
    }

    private WebChatNonceString getNonceString() {
        return Optional.ofNullable(nonceString)
                .orElse(defaultNonceString);
    }

    private WebChatTimestamp getTimestamp() {
        return Optional.ofNullable(timestamp)
                .orElse(defaultTimestamp);
    }

    @Override
    public WebChatClient.Builder webChatProperties(WebChatProperties webChatProperties) {
        this.properties = Objects.requireNonNull(webChatProperties);
        return this;
    }
}
