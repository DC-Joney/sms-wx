package com.security.demo.test;

import lombok.extern.log4j.Log4j2;
import reactor.core.Exceptions;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log4j2
public abstract class ResultApply<T> {

    public static <T> ResultApply<T> just(T data) {
        return new ResultJust<>(data);
    }

    public <R> ResultApply<R> resultMap(Function<? super T, ? extends R> function) {
        return ResultApply.just(function.apply(getData()));
    }

    public abstract T getData();


    public void doOnext(Consumer<? super T> consumer) {
        consumer.accept(getData());
    }


    static class ResultJust<T> extends ResultApply<T> {
        private final T data;

        public ResultJust(T data) {
            this.data = data;
        }

        @Override
        public T getData() {
            return data;
        }
    }





    public static  Stream<Integer> generateData(){
         return IntStream.range(1,10)
                .boxed();
    }


//    public static void main(String[] args) throws InterruptedException {
//
////        Flux.fromStream(IntStream.range(1,10).boxed())
////                .map(String::valueOf)
////                .subscribe();
////
////
////        ResultApply.just("1111")
////                .resultMap(String::toUpperCase)
////                .doOnext(log::info);
////
////        Context of = Context.of("1", "2");
////        of.getOrEmpty("2")
////                .ifPresent(log::info);
////
////
////        Mono.ignoreElements(Mono.justOrEmpty("22222"))
////                .subscribe(log::info);
//
//
//
//         Mono.first(Mono.justOrEmpty("111"),Mono.justOrEmpty("222"))
//                 .subscribe(log::info);
//         Mono.fromDirect(Flux.empty()).subscribe(log::info);
//
//         Mono.from(Mono.empty()).subscribe(log::info);
//
//
////         Mono.zip(Mono.just("1111"),Mono.error(new RuntimeException()))
////                 .subscribe(log::info);
//
//        Hooks.onOperatorDebug();
//
//         Mono.justOrEmpty("222")
//                 .materialize()
//                 .map(Signal::isOnNext)
//                 .log()
//                 .subscribe(log::info);
////       Flux.create(fluxSink -> {
////           fluxSink.next("111")
////                   .onRequest(log::info)
////                   .onDispose()
////       })
//        Scheduler immediate = Schedulers.immediate();
////        immediate.start();
////        immediate.schedule(()-> log.info("1111"),3, TimeUnit.SECONDS);
////        immediate.createWorker().dispose();
//        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);
//        executorService.schedule(()-> log.info("1111"),3, TimeUnit.SECONDS);
//        executorService.shutdown();
//
//
//        Exceptions.isErrorCallbackNotImplemented(new RuntimeException());
//
//        Flux.just("timeout1")
//                .flatMap(identity())
//                .onErrorResume(original -> Flux.error(
//                        new RuntimeException("oops, SLA exceeded", original)
//                )).subscriberContext(context -> {
//                    log.info(context);
//                    return context;
//        }).subscribe();
//
//
//        Flux<String> flux =
//                Flux.interval(Duration.ofMillis(250))
//                        .map(input -> {
//                            if (input < 3) return "tick " + input;
//                            throw new RuntimeException("boom");
//                        })
//                        .onErrorReturn("Uh oh");
//
//        flux.subscribe(System.out::println);
//
//
//
//        Flux.interval(Duration.ofMillis(250))
//                .map(input -> {
//                    if (input < 3) return "tick " + input;
//                    throw new RuntimeException("boom");
//                })
//                .elapsed()
//                .retry(1)
//                .subscribe(log::info,log::info);
//
//        Thread.sleep(2100);
//
//
////        ConnectableFlux connectableFlux = ConnectableFlux.interval();
//        connectableFlux.connect().dispose();
//    }


    public static <R> Function<R,Mono<R>> identity(){
        return Mono::justOrEmpty;
    }

}
