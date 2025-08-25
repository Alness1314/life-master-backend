package com.alness.lifemaster.income.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class IncomeSpecification implements Specification<IncomeEntity> {

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<IncomeEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<IncomeEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<IncomeEntity> specification = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<IncomeEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "source" -> filterBySource(entry.getValue());
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

    private Specification<IncomeEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<IncomeEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<IncomeEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<IncomeEntity> filterBySource(String source) {
        return (root, query, cb) -> cb.equal(root.<String>get("source"), source);

    }

}
