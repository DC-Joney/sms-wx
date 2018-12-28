package com.security.demo.webchat.filter;

import com.security.demo.webchat.exception.AuthenticationMethodNotSupport;
import com.security.demo.webchat.support.WebChatAuthenticationMethod;
import com.security.demo.webchat.support.WebChatParameterNames;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class WebChatAccessTokenResponseClient implements ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private WebClient webClient = WebClient.builder()
            .build();


    @Override
    public Mono<OAuth2AccessTokenResponse> getTokenResponse(OAuth2ClientCredentialsGrantRequest authorizationGrantRequest) {
        return Mono.defer(() -> {

            ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();

            String tokenUrl = url(authorizationGrantRequest);

            return this.webClient
                    .get()
                    .uri(tokenUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .flatMap(response -> {
                        if (!response.statusCode().is2xxSuccessful()) {
                            // extract the contents of this into a method named oauth2AccessTokenResponse but has an argument for the response
                            throw WebClientResponseException.create(response.rawStatusCode(),
                                    "Cannot get token, expected 2xx HTTP Status code",
                                    null,
                                    null,
                                    null
                            );
                        }
                        return response.body(WebChatOAuth2BodyExtractors.oauth2AccessTokenResponse());
                    });
        });
    }



    private static String url(OAuth2ClientCredentialsGrantRequest authorizationGrantRequest) {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
        AuthorizationGrantType grantType = authorizationGrantRequest.getGrantType();
        return Optional.of(clientRegistration)
                .filter(registration -> WebChatAuthenticationMethod.GET.equals(registration.getClientAuthenticationMethod()))
                .map(registration -> WebChatAuthenticationMethod.urlBuilder(()-> registration.getProviderDetails().getTokenUri())
                        .addParamter(WebChatParameterNames.GRANT_TYPE, grantType.getValue()
                                .substring(0,grantType.getValue().length() - 1))
                        .addParamter(WebChatParameterNames.APP_ID, registration.getClientId())
                        .addParamter(WebChatParameterNames.APP_SECRET, registration.getClientSecret())
                        .buildUrl())
                .orElseThrow(() -> new AuthenticationMethodNotSupport("AuthenticationMethod is not support : "
                                                    + clientRegistration.getClientAuthenticationMethod().getValue()));
    }


}
