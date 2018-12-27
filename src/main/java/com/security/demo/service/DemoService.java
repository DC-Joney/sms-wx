package com.security.demo.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DemoService {
    public Optional<UserDetails> findUser(String userName){
        return Optional.of(User.withUsername(userName).password("admin").roles("ADMIN").build());
    }
}
