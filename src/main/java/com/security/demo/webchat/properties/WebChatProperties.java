package com.security.demo.webchat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.wx.client")
public class WebChatProperties {

    private String registrationId;

    private String clientId;

    private String clientSecret;

    private String tokenUrl;

    private String ticketUrl;
}
