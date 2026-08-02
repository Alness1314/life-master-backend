package com.alness.lifemaster.common.specification;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

public final class FilterSpecifications {
    private FilterSpecifications() {
    }

    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + escaped + "%", '\\');
    }
}
