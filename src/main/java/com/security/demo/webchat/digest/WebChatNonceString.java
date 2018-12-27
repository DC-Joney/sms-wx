package com.security.demo.webchat.digest;

import java.util.UUID;

@FunctionalInterface
public interface WebChatNonceString {

   String nonceStr();

    static WebChatNonceString defaultNonceStr(){
        return () -> UUID.randomUUID().toString();
    }


}
