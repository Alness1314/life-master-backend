package com.alness.lifemaster.exercises.spec;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.exercises.entity.ExercisesEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ExercisesSpec implements Specification<ExercisesEntity> {

    @SuppressWarnings("null")
    @Override
    @Nullable
    public Predicate toPredicate(Root<ExercisesEntity> root, @Nullable CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<ExercisesEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<ExercisesEntity> specification = notErased();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<ExercisesEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "date", "trainingDate" -> exactDate(entry.getValue());
                case "activityType" -> containsIgnoreCase("activityType", entry.getValue());
                case "startTime", "endTime" -> exactTime(entry.getKey(), entry.getValue());
                case "durationMinutes" -> exactDuration(entry.getValue());
                case "user" -> filterByUser(entry.getValue());
                default -> null;
            };
            if (currentFilter != null) {
                specification = specification.and(currentFilter);
            }
        }
        return specification;
    }

    private Specification<ExercisesEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<ExercisesEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<ExercisesEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<ExercisesEntity> exactDate(String date) {
        return (root, query, cb) -> cb.equal(root.<LocalDate>get("trainingDate"), LocalDate.parse(date));
    }

    private Specification<ExercisesEntity> exactTime(String field, String value) {
        return (root, query, cb) -> cb.equal(root.<LocalTime>get(field), LocalTime.parse(value));
    }

    private Specification<ExercisesEntity> exactDuration(String value) {
        return (root, query, cb) -> cb.equal(root.<Integer>get("durationMinutes"), Integer.valueOf(value));
    }

    private Specification<ExercisesEntity> notErased() {
        return (root, query, cb) -> cb.isFalse(root.get("erased"));
    }

}
