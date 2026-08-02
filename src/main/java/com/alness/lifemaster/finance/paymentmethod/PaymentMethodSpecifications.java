package com.alness.lifemaster.finance.paymentmethod;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

final class PaymentMethodSpecifications {
    private PaymentMethodSpecifications() {
    }

    static Specification<PaymentMethodEntity> from(UUID userId, Map<String, String> filters) {
        Specification<PaymentMethodEntity> specification = ownedAndActive(userId);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<PaymentMethodEntity> current = switch (entry.getKey()) {
                case "name" -> containsIgnoreCase("name", entry.getValue());
                case "methodType" -> exact("methodType", PaymentMethodType.valueOf(entry.getValue()));
                case "active" -> exact("active", Boolean.valueOf(entry.getValue()));
                default -> null;
            };
            if (current != null) {
                specification = specification.and(current);
            }
        }
        return specification;
    }

    private static Specification<PaymentMethodEntity> ownedAndActive(UUID userId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.isFalse(root.get("erased")));
    }

    private static <V> Specification<PaymentMethodEntity> exact(String field, V value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }
}
