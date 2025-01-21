package com.alness.lifemaster.vault.specification;

import java.util.Map;
import java.util.Map.Entry;
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

    @Override
    public Predicate toPredicate(Root<VaultEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<VaultEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<VaultEntity> specification = Specification.where(null);
        for (Entry<String, String> entry : params.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                    specification = specification.and(this.filterById(entry.getValue()));
                    break;
                case "source":
                    specification = specification.and(this.filterBySource(entry.getValue()));
                    break;
                case "user":
                    specification = specification.and(this.filterByUser(entry.getValue()));
                    break;
                default:
                    break;
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

    private Specification<VaultEntity> filterBySource(String source) {
        return (root, query, cb) -> cb.equal(root.<String>get("source"), source);

    }
    
}
