package com.security.demo.webchat.client;

import com.security.demo.webchat.WebChatDto;
import com.security.demo.webchat.digest.WebChatNonceString;
import com.security.demo.webchat.digest.WebChatTimestamp;
import com.security.demo.webchat.properties.WebChatProperties;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public interface WebChatClient{

    static  Builder webClient(WebClient webClient){
        return new DefaultWebChatClientBuilder(webClient);
    }

    Mono<WebChatDto> toWebChatDto(String pageUrl);

    WebChatClient appId(String appId);

    interface Builder{

        Builder webChatProperties(WebChatProperties webChatProperties);

        Builder nonceString(WebChatNonceString nonceString);

        Builder timestamp(WebChatTimestamp timestamp);

        WebChatClient build();
    }

}
