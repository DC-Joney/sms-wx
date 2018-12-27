package com.security.demo.secuirty.support;

import com.security.demo.secuirty.strategy.DefaultJsonStrategy;
import com.security.demo.secuirty.strategy.JsonStrategy;
import com.security.demo.secuirty.utils.JsonConvertUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class JsonServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    private JsonStrategy jsonStrategy;

    public JsonServerAuthenticationFailureHandler(JsonStrategy jsonStrategy){
        this.jsonStrategy = Optional.ofNullable(jsonStrategy).orElseGet(DefaultJsonStrategy::new);
    }


    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException authenticationException) {
        return Mono.fromCompletionStage(JsonConvertUtils.convertToString(authenticationException))
                .flatMap(authStr-> jsonStrategy.writeResponse(webFilterExchange.getExchange(),authStr));
    }


    public void setJsonStrategy(JsonStrategy jsonStrategy) {
        this.jsonStrategy = jsonStrategy;
    }
}
