package com.alness.lifemaster.finance.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

final class RecurringMovementSpecifications {
    private RecurringMovementSpecifications() {
    }

    static Specification<RecurringMovementEntity> from(UUID userId, Map<String, String> filters) {
        Specification<RecurringMovementEntity> specification = ownedAndActive(userId);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<RecurringMovementEntity> current = switch (entry.getKey()) {
                case "description" -> containsIgnoreCase("description", entry.getValue());
                case "categoryName" -> relatedContains("category", "name", entry.getValue());
                case "movementType" -> exact("movementType", MovementType.valueOf(entry.getValue()));
                case "frequency" -> exact("frequency", RecurrenceFrequency.valueOf(entry.getValue()));
                case "amount" -> exact("amount", new BigDecimal(entry.getValue()));
                case "startDate", "endDate", "nextExecutionDate" ->
                    exact(entry.getKey(), LocalDate.parse(entry.getValue()));
                case "active" -> exact("active", Boolean.valueOf(entry.getValue()));
                default -> null;
            };
            if (current != null) {
                specification = specification.and(current);
            }
        }
        return specification;
    }

    private static Specification<RecurringMovementEntity> ownedAndActive(UUID userId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.isFalse(root.get("erased")));
    }

    private static <V> Specification<RecurringMovementEntity> exact(String field, V value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private static Specification<RecurringMovementEntity> relatedContains(
            String relation, String field, String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return (root, query, cb) -> cb.like(
                cb.lower(root.join(relation).get(field)), "%" + escaped + "%", '\\');
    }
}
