package com.alness.lifemaster.expenses.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ExpensesSpecification implements Specification<ExpensesEntity> {

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<ExpensesEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<ExpensesEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<ExpensesEntity> specification = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<ExpensesEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "bank" -> filterByBank(entry.getValue());
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

    private Specification<ExpensesEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<ExpensesEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<ExpensesEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<ExpensesEntity> filterByBank(String bank) {
        return (root, query, cb) -> cb.equal(root.<String>get("bankOrEntity"), bank);

    }

}
