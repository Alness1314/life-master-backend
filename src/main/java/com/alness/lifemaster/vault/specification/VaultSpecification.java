package com.alness.lifemaster.vault.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.vault.entity.VaultEntity;

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
        Specification<VaultEntity> specification = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<VaultEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "site" -> filterBySite(entry.getValue());
                case "user" -> filterByUser(entry.getValue());
                default -> null;
            };

            if (currentFilter != null) {
                specification = (specification == null)
                        ? currentFilter
                        : specification.and(currentFilter);
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

    private Specification<VaultEntity> filterBySite(String siteName) {
        return (root, query, cb) -> cb.equal(root.<String>get("siteName"), siteName);

    }

}
