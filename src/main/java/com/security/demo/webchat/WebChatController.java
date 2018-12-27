package com.security.demo.webchat;

import com.security.demo.webchat.client.WebChatClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
public class WebChatController {

    @Autowired
    private WebChatClient webChatClient;

    @RequestMapping("/search/wx")
    public Mono<WebChatDto> searchWebChat(@RequestParam("pageUrl") String pageUrl){
        return webChatClient.toWebChatDto(pageUrl);
    }
}
