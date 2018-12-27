package com.security.demo.webchat.exception;

public class AuthenticationMethodNotSupport extends RuntimeException {

    public AuthenticationMethodNotSupport(String message) {
        super(message);
    }
}
