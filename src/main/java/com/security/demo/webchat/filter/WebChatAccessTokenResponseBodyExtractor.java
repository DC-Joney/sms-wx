package com.security.demo.webchat.filter;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import reactor.core.publisher.Mono;

import java.util.*;

public class WebChatAccessTokenResponseBodyExtractor implements BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> {

    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    WebChatAccessTokenResponseBodyExtractor() {}

    @Override
    public Mono<OAuth2AccessTokenResponse> extract(ReactiveHttpInputMessage inputMessage,
                                                   Context context) {
        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {};
        BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors.toMono(type);
        return delegate.extract(inputMessage, context)
                .map(WebChatAccessTokenResponseBodyExtractor::parse)
                .flatMap(WebChatAccessTokenResponseBodyExtractor::oauth2AccessTokenResponse)
                .map(WebChatAccessTokenResponseBodyExtractor::oauth2AccessTokenResponse);
    }

    private static TokenResponse parse(Map<String, Object> json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.appendField("token_type", AccessTokenType.BEARER.getValue());
            return TokenResponse.parse(jsonObject);
        }
        catch (ParseException pe) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                    "An error occurred parsing the Access Token response: " + pe.getMessage(), null);
            throw new OAuth2AuthorizationException(oauth2Error, pe);
        }
    }

    private static Mono<AccessTokenResponse> oauth2AccessTokenResponse(TokenResponse tokenResponse) {
        if (tokenResponse.indicatesSuccess()) {
            return Mono.just(tokenResponse)
                    .cast(AccessTokenResponse.class);
        }
        TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
        ErrorObject errorObject = tokenErrorResponse.getErrorObject();
        OAuth2Error oauth2Error;
        if (errorObject == null) {
            oauth2Error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR);
        } else {
            oauth2Error = new OAuth2Error(
                    errorObject.getCode() != null ? errorObject.getCode() : OAuth2ErrorCodes.SERVER_ERROR,
                    errorObject.getDescription(),
                    errorObject.getURI() != null ? errorObject.getURI().toString() : null);
        }
        return Mono.error(new OAuth2AuthorizationException(oauth2Error));
    }

    private static OAuth2AccessTokenResponse oauth2AccessTokenResponse(AccessTokenResponse accessTokenResponse) {
        AccessToken accessToken = accessTokenResponse.getTokens().getAccessToken();
        OAuth2AccessToken.TokenType accessTokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue()
                .equalsIgnoreCase(accessToken.getType().getValue())) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        }
        long expiresIn = accessToken.getLifetime();

        String refreshToken = null;
        if (accessTokenResponse.getTokens().getRefreshToken() != null) {
            refreshToken = accessTokenResponse.getTokens().getRefreshToken().getValue();
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(accessTokenResponse.getCustomParameters());

        return OAuth2AccessTokenResponse.withToken(accessToken.getValue())
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .additionalParameters(additionalParameters)
                .build();
    }
}
