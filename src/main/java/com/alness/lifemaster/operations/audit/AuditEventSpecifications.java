package com.alness.lifemaster.operations.audit;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

final class AuditEventSpecifications {
    private AuditEventSpecifications() { }

    static Specification<AuditEventEntity> userId(UUID userId) {
        return userId == null ? null : (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    static Specification<AuditEventEntity> text(String field, String value) {
        return value == null || value.isBlank() ? null : (root, query, cb) ->
                cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase() + "%");
    }

    static Specification<AuditEventEntity> successful(Boolean value) {
        return value == null ? null : (root, query, cb) -> cb.equal(root.get("successful"), value);
    }

    static Specification<AuditEventEntity> from(LocalDate value) {
        return value == null ? null : (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), value.atStartOfDay());
    }

    static Specification<AuditEventEntity> to(LocalDate value) {
        return value == null ? null : (root, query, cb) ->
                cb.lessThan(root.get("createdAt"), value.plusDays(1).atStartOfDay());
    }
}
