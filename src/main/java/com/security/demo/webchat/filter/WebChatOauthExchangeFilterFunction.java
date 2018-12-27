package com.security.demo.webchat.filter;

import com.security.demo.webchat.support.WebChatAuthenticationMethod;
import com.security.demo.webchat.support.TypeTicket;
import com.security.demo.webchat.support.WebChatParameterNames;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

@Log4j2
public class WebChatOauthExchangeFilterFunction implements ExchangeFilterFunction {

    private ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> responseClient;

    private ReactiveClientRegistrationRepository registrationRepository;

    private String defaultClientRegistrationId;


    public WebChatOauthExchangeFilterFunction(ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> responseClient,
                                              ReactiveClientRegistrationRepository registrationRepository){
        this.responseClient = responseClient;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return registrationRepository.findByRegistrationId(getDefaultClientRegistrationId())
                .map(OAuth2ClientCredentialsGrantRequest::new)
                .flatMap(responseClient::getTokenResponse)
                .map(response -> uri(request, response))
                .flatMap(next::exchange);
    }


    private ClientRequest uri(ClientRequest request, OAuth2AccessTokenResponse tokenResponse) {
        ClientRequest build = ClientRequest.from(request)
                .url(computedUrl(request, tokenResponse))
                .build();
        return build;
    }


    private URI computedUrl(ClientRequest request, OAuth2AccessTokenResponse tokenResponse){
        String  ticketUrl = WebChatAuthenticationMethod.urlBuilder(request.url())
                .addParamter(WebChatParameterNames.ACCESS_TOKEN, tokenResponse.getAccessToken().getTokenValue())
                .addParamter(WebChatParameterNames.ACCESS_TYPE, TypeTicket.JSAPI.getValue())
                .buildUrl();
        return URI.create(ticketUrl);
    }


    private String getDefaultClientRegistrationId() {
        return defaultClientRegistrationId;
    }


    public void setDefaultClientRegistrationId(String defaultClientRegistrationId) {
        this.defaultClientRegistrationId = defaultClientRegistrationId;
    }

}
