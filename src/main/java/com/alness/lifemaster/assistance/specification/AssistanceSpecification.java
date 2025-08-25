package com.alness.lifemaster.assistance.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class AssistanceSpecification implements Specification<AssistanceEntity> {

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<AssistanceEntity> arg0, CriteriaQuery<?> arg1, CriteriaBuilder arg2) {
        return null;
    }

    public Specification<AssistanceEntity> getSpecificationByFilters(Map<String, String> params) {
        Specification<AssistanceEntity> specification = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<AssistanceEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
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

    private Specification<AssistanceEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<AssistanceEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<AssistanceEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }
}
