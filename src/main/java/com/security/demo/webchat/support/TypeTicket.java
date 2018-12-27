package com.security.demo.webchat.support;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum  TypeTicket {

    JSAPI("jsapi");

    private static final Map<String, TypeTicket> mappings = new HashMap<>(16);

    private final String value;

    static {
        for (TypeTicket ticket : values()) {
            mappings.put(ticket.name(), ticket);
        }
    }

    TypeTicket(String name){
        this.value = name;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public static TypeTicket resolve(@Nullable String ticket) {
        return (ticket != null ? mappings.get(ticket) : null);
    }

    public boolean matches(String ticket) {
        return (this == resolve(ticket));
    }
}
