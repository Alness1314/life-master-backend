package com.alness.lifemaster.utils;

import java.util.HashMap;
import java.util.Map;

public class ContainerUtil {
    private Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(data.get(key)); // Seguridad de tipos con cast seguro
    }
}
