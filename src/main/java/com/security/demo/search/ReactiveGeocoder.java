package com.security.demo.search;


import reactor.core.publisher.Mono;

import java.util.Map;

public interface ReactiveGeocoder<K,V> {

    <K,V> Mono<String> straightGeocoder(Map<K,V> mapParams);


    <K,V> Mono<String> reverseGeocoder(Map<K,V> mapParams);






}
