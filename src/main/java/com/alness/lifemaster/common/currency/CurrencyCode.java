package com.alness.lifemaster.common.currency;

public enum CurrencyCode {
    MXN("Peso mexicano"),
    USD("Dólar estadounidense"),
    EUR("Euro");

    private final String name;

    CurrencyCode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static boolean supports(String value) {
        if (value == null) {
            return false;
        }
        try {
            valueOf(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
