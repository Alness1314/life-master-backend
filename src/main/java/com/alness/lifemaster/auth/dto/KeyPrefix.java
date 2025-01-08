package com.alness.lifemaster.auth.dto;

public class KeyPrefix {
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String CHAR_ENCODING = "UTF-8";

    private KeyPrefix() {
        throw new IllegalStateException("Utility class");
    }
}
