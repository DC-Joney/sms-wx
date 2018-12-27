package com.security.demo.test;

import java.net.URI;

public class URLTest {
    public static void main(String[] args) {
        URI uri = URI.create("https://www.baidu.com");
        System.out.println(uri.resolve("?aaa=222"));
    }
}
