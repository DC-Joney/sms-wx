package com.security.demo.webchat.client;

import com.security.demo.webchat.WebChatDto;
import com.security.demo.webchat.digest.WebChatDigestSign;
import com.security.demo.webchat.digest.WebChatNonceString;
import com.security.demo.webchat.digest.WebChatTimestamp;
import com.security.demo.webchat.exception.PageUrlEmpty;
import com.security.demo.webchat.exception.TicketNotFoundException;
import com.security.demo.webchat.properties.WebChatProperties;
import com.security.demo.webchat.support.WebChatParameterNames;
import lombok.Data;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Map;

public class DefaultWebChatClient implements WebChatClient {

    private WebClient webClient;

    private WebChatNonceString nonceString;

    private WebChatTimestamp timestamp;

    private WebChatProperties properties;

    private String appId;

    DefaultWebChatClient(WebChatProperties properties, WebClient webClient,
                         WebChatNonceString nonceString, WebChatTimestamp timestamp) {

        this.webClient = webClient;
        this.nonceString = nonceString;
        this.timestamp = timestamp;
        this.properties = properties;
        this.appId = properties.getClientId();
    }


    @Override
    public WebChatClient appId(String appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public Mono<WebChatDto> toWebChatDto(String pageUrl) {
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
                    return webChatSign(response, pageUrl);
                });
    }


    private Mono<WebChatDto> webChatSign(ClientResponse clientResponse, String pageUrl) {

        Mono<String> pageUrlMono = Mono.justOrEmpty(pageUrl)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new PageUrlEmpty("The page url not empty"))));

        Mono<String> responseMono = Mono.justOrEmpty(clientResponse)
                .flatMap(response -> response.body(bodyExtractor()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TicketNotFoundException("The jsapi_ticket is not found, please check your params"))));

        return Mono.zip(pageUrlMono, responseMono)
                .map(s -> WebChatRequest.builder().nonceStr(nonceString.nonceStr())
                        .timestamp(timestamp.timestamp().getLong(ChronoField.INSTANT_SECONDS)).url(s.getT1())
                        .jsTicket(s.getT2()).appId(appId).build())
                .flatMap(WebChatDigestSign::digestSign);
    }

//   JSONObjectUtils
    private BodyExtractor<Mono<String>, ReactiveHttpInputMessage> bodyExtractor() {
        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {};
        BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors.toMono(type);
        return (inputMessage, context) ->
                delegate.extract(inputMessage, context)
                        .map(json -> {
                            JSONObject jsonObject = new JSONObject(json);
                            return jsonObject.getAsString(WebChatParameterNames.TICKET_JSON_NAME);
                        });
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
