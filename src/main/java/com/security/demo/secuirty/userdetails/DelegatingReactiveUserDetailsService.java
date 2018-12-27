package com.security.demo.secuirty.userdetails;

import com.security.demo.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;


public class DelegatingReactiveUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private DemoService demoService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.justOrEmpty(demoService.findUser(username));
    }
}
