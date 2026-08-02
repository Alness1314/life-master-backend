package com.alness.lifemaster.income.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

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

        Specification<IncomeEntity> specification = notErased();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<IncomeEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "source", "description" -> containsIgnoreCase(entry.getKey(), entry.getValue());
                case "amount" -> exactAmount(entry.getValue());
                case "paymentDate" -> exactDate(entry.getValue());
                case "currency" -> containsIgnoreCase("currency", entry.getValue());
                case "user" -> filterByUser(entry.getValue());
                default -> null;
            };
            if (currentFilter != null) {
                specification = specification.and(currentFilter);
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

    private Specification<IncomeEntity> exactAmount(String amount) {
        return (root, query, cb) -> cb.equal(root.<BigDecimal>get("amount"), new BigDecimal(amount));
    }

    private Specification<IncomeEntity> exactDate(String date) {
        return (root, query, cb) -> cb.equal(root.<LocalDate>get("paymentDate"), LocalDate.parse(date));
    }

    private Specification<IncomeEntity> notErased() {
        return (root, query, cb) -> cb.isFalse(root.get("erased"));
    }

}
