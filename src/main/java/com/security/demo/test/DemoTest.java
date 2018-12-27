package com.security.demo.test;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Subscription;
import org.springframework.util.StringUtils;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Log4j2
public class DemoTest {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.range(1,20)
                .bufferTimeout(5,Duration.ofSeconds(2),Schedulers.parallel())
                .delaySubscription(Duration.ofSeconds(0))
                .delayElements(Duration.ofSeconds(2),Schedulers.newParallel("Thread"))
                .reduce((integers, integers2) -> {
                    integers.addAll(integers2);
                    return integers;
                })
//                .delayElements(Duration.ofSeconds(2))
                .doOnRequest(log::info)
                .subscribe(log::info);

        countDownLatch.await(10, TimeUnit.SECONDS);
    }
}
