package com.security.demo.secuirty.support;

import com.security.demo.secuirty.strategy.DefaultJsonStrategy;
import com.security.demo.secuirty.strategy.JsonStrategy;
import com.security.demo.secuirty.utils.JsonConvertUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class JsonServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private JsonStrategy jsonStrategy;

    public JsonServerAuthenticationSuccessHandler(JsonStrategy jsonStrategies){
        this.jsonStrategy = Optional.ofNullable(jsonStrategies).orElseGet(DefaultJsonStrategy::new);
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return Mono.fromCompletionStage(JsonConvertUtils.convertToString(authentication))
                .flatMap(authStr-> jsonStrategy.writeResponse(webFilterExchange.getExchange(),authStr));
    }

    public void setJsonStrategy(JsonStrategy jsonStrategy) {
        this.jsonStrategy = jsonStrategy;
    }
}
