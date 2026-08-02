package com.alness.lifemaster.finance.account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

final class FinancialAccountSpecifications {
    private FinancialAccountSpecifications() {
    }

    static Specification<FinancialAccountEntity> from(UUID userId, Map<String, String> filters) {
        Specification<FinancialAccountEntity> specification = ownedAndActive(userId);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<FinancialAccountEntity> current = switch (entry.getKey()) {
                case "name" -> containsIgnoreCase("name", entry.getValue());
                case "accountType" -> exact("accountType", AccountType.valueOf(entry.getValue()));
                case "currency" -> containsIgnoreCase("currency", entry.getValue());
                case "initialBalance" -> exact("initialBalance", new BigDecimal(entry.getValue()));
                case "active" -> exact("active", Boolean.valueOf(entry.getValue()));
                default -> null;
            };
            if (current != null) {
                specification = specification.and(current);
            }
        }
        return specification;
    }

    private static Specification<FinancialAccountEntity> ownedAndActive(UUID userId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.isFalse(root.get("erased")));
    }

    private static <V> Specification<FinancialAccountEntity> exact(String field, V value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }
}
