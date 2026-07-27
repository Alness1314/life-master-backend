package com.alness.lifemaster.common.dto;

import java.util.Map;

import com.alness.lifemaster.common.validation.ValueExistence;

public record ValueExistenceResponse(
        boolean success,
        String message,
        Map<String, ValueExistence> data) {
}
