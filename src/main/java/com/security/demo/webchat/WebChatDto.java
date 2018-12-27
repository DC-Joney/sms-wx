package com.security.demo.webchat;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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

}
