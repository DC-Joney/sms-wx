package com.security.demo.webchat;

import com.security.demo.webchat.client.WebChatClient;
import com.security.demo.webchat.filter.WebChatAccessTokenResponseClient;
import com.security.demo.webchat.filter.WebChatOauthExchangeFilterFunction;
import com.security.demo.webchat.properties.WebChatProperties;
import com.security.demo.webchat.support.WebChatAuthenticationMethod;
import com.security.demo.webchat.support.WebChatParameterNames;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Configuration
@EnableConfigurationProperties(WebChatProperties.class)
public class WebChatConfiguration {

    private WebChatProperties properties;

    public WebChatConfiguration(WebChatProperties webChatProperties){
        this.properties = webChatProperties;
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(properties.getRegistrationId())
                .clientAuthenticationMethod(WebChatAuthenticationMethod.GET)
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(properties.getTokenUrl())
                .build();
        return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
    }

    @Bean
    public ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> reactiveOAuth2AccessTokenResponseClient(){
        return new WebChatAccessTokenResponseClient();
    }

    @Bean
    public WebChatOauthExchangeFilterFunction webChatOauthExchangeFilterFunction(ReactiveClientRegistrationRepository registrationRepository){
        WebChatOauthExchangeFilterFunction filterFunction = new WebChatOauthExchangeFilterFunction(reactiveOAuth2AccessTokenResponseClient(),
                registrationRepository);
        filterFunction.setDefaultClientRegistrationId(properties.getRegistrationId());
        return filterFunction;
    }


    @Bean(name = WebChatParameterNames.DEFAULT_WEBCLIENT_BEAN_NAME)
    @ConditionalOnMissingBean(name = WebChatParameterNames.DEFAULT_WEBCLIENT_BEAN_NAME)
    public WebClient webClient(ReactiveClientRegistrationRepository registrationRepository){
        return WebClient.builder().filter(webChatOauthExchangeFilterFunction(registrationRepository)).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebChatClient webChatClient(@Qualifier(WebChatParameterNames.DEFAULT_WEBCLIENT_BEAN_NAME) WebClient webClient){
        return WebChatClient.webClient(webClient).webChatProperties(properties).build();
    }
}
