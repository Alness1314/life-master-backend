package com.alness.lifemaster.vault.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.vault.entity.VaultEntity;
import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class VaultSpecification implements Specification<VaultEntity> {

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<VaultEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<VaultEntity> getSpecificationByFilters(Map<String, String> params) {
        Specification<VaultEntity> specification = notErased();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<VaultEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "site", "siteName" -> containsIgnoreCase("siteName", entry.getValue());
                case "siteUrl", "username" -> containsIgnoreCase(entry.getKey(), entry.getValue());
                case "user" -> filterByUser(entry.getValue());
                default -> null;
            };

            if (currentFilter != null) {
                specification = specification.and(currentFilter);
            }
        }
        return specification;
    }

    private Specification<VaultEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<VaultEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<VaultEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<VaultEntity> notErased() {
        return (root, query, cb) -> cb.isFalse(root.get("erased"));
    }

}
