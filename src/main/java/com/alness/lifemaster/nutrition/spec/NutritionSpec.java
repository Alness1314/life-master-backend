package com.alness.lifemaster.nutrition.spec;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.nutrition.entity.NutritionEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.utils.DateTimeUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class NutritionSpec implements Specification<NutritionEntity> {

    @SuppressWarnings("null")
    @Override
    @Nullable
    public Predicate toPredicate(Root<NutritionEntity> root, @Nullable CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<NutritionEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<NutritionEntity> specification = Specification.where(null);
        for (Entry<String, String> entry : params.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                    specification = specification.and(this.filterById(entry.getValue()));
                    break;
                case "date":
                    specification = specification.and(this.filterByDate(entry.getValue()));
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

    private Specification<NutritionEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<NutritionEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<NutritionEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<NutritionEntity> filterByDate(String date) {
        return (root, query, cb) -> cb.equal(root.<LocalDateTime>get("dateTimeConsumption"),
                DateTimeUtils.parseToLocalDateTime(date));

    }

}
