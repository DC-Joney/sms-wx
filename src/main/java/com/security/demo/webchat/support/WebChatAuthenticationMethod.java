package com.security.demo.webchat.support;

import com.security.demo.webchat.exception.UrlEmptyException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public interface WebChatAuthenticationMethod {

    ClientAuthenticationMethod GET = new ClientAuthenticationMethod("get");


    static URLBuilder urlBuilder(String baseUrl) {
        return new URLBuilder(nonNull(baseUrl), true);
    }


    static URLBuilder urlBuilder(URI baseUrl) {
        return new URLBuilder(nonNull(baseUrl).toString(), true);
    }


    static URLBuilder urlBuilder(String baseUrl, boolean baseState) {
        return new URLBuilder(nonNull(baseUrl), baseState);
    }


    static <T> T nonNull(T t) {
        return Objects.requireNonNull(t);
    }


    class URLBuilder {

        private static final String DEFAULT_SEPARATOR = "&";

        private final StringBuffer stringBuffer = new StringBuffer();

        private boolean beginState;

        //是否为baseUrl
        private final boolean baseState;

        private URLBuilder(String baseUrl, boolean baseState) {
            this.baseState = baseState;
            this.beginState = true;
            stringBuffer.append(computeBaseUrl(baseUrl));

        }

        public URLBuilder addParamter(String name, String value) {
            Optional.of(stringBuffer)
                    .filter(buffer -> beginState)
                    .map(this::changeState)
                    .orElseGet(() -> stringBuffer.append(DEFAULT_SEPARATOR))
                    .append(name).append("=").append(value);
            return this;
        }

        public String buildUrl() {
            return stringBuffer.toString();
        }

        private StringBuffer changeState(StringBuffer stringBuffer) {
            beginState = false;
            return stringBuffer;
        }

        private String computeBaseUrl(String baseUrl) {
            return Optional.ofNullable(baseUrl)
                    .filter(StringUtils::hasText)
                    .map(this::computeUrl)
                    .orElseThrow(() -> new UrlEmptyException("The url must be not null or empty"));
        }

        private String computeUrl(String url) {
            return Optional.of(url)
                    .filter(u -> baseState)
                    .map(u -> u + "?")
                    .orElse(url + DEFAULT_SEPARATOR);
        }
    }
}
