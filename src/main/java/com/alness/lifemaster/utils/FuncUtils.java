package com.alness.lifemaster.utils;

import java.util.HashMap;
import java.util.Map;

import com.alness.lifemaster.common.keys.Filters;

public class FuncUtils {

    private FuncUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> integrateUser(String userId, Map<String, String> params) {
        Map<String, String> updatedParams = new HashMap<>(params);
        updatedParams.put(Filters.KEY_USER, userId);
        return updatedParams;
    }
}
