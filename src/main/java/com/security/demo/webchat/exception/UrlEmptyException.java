package com.security.demo.webchat.exception;

public class UrlEmptyException extends RuntimeException {

    public UrlEmptyException(String message) {
        super(message);
    }
}
