package com.alness.lifemaster.common.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.alness.lifemaster.common.dto.ValueExistenceResponse;

@Component
public class GenericExistenceValidator {

    public ValueExistenceResponse validate(
            Map<String, String> values,
            Map<String, Predicate<String>> fieldValidators) {

        if (values == null || values.isEmpty()
                || values.values().stream().allMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Debe enviar al menos un campo para validar.");
        }

        Set<String> unsupportedFields = new java.util.HashSet<>(values.keySet());
        unsupportedFields.removeAll(fieldValidators.keySet());
        if (!unsupportedFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campos no permitidos para validación: " + String.join(", ", unsupportedFields));
        }

        Map<String, ValueExistence> result = new LinkedHashMap<>();
        values.forEach((field, value) -> {
            if (value != null && !value.isBlank()) {
                boolean exists = fieldValidators.get(field).test(value.trim());
                result.put(field, exists ? ValueExistence.EXISTS : ValueExistence.NOT_EXIST);
            }
        });

        return new ValueExistenceResponse(
                true,
                "Consulta de valores únicos realizada correctamente.",
                result);
    }
}
