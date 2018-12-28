package com.security.demo.webchat;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;

@Data
@Builder
public class WebChatDto {

    //公众号的唯一标识
    private String appId;

    //生成签名的时间戳
    private long timeStamp;

    //随即字符串
    private String noncestr;

    //签名
    private String signature;

    private long expireTime;

    public long getExpireTime() {
        return !Instant.ofEpochSecond(expireTime).isAfter(Instant.now()) ? Instant.EPOCH.getEpochSecond() :
                Duration.ofSeconds(expireTime).minusSeconds(Instant.now().getEpochSecond()).getSeconds();
    }
}
