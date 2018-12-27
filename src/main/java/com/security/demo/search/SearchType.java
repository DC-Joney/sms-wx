package com.security.demo.search;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum SearchType {

    BAIDU,

    GAODE;

    private static final Map<String, SearchType> mappings = new HashMap<>(16);

    static {
        for (SearchType searchArea : values()) {
            mappings.put(searchArea.name(), searchArea);
        }
    }

    private SearchType() {}


    @Nullable
    public static SearchType resolve(@Nullable String method) {
        return (method != null ? mappings.get(method) : null);
    }

    public boolean matches(String type) {
        return (this == resolve(type));
    }
}
