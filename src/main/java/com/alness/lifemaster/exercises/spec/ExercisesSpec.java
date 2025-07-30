package com.alness.lifemaster.exercises.spec;

import java.time.LocalDate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.exercises.entity.ExercisesEntity;
import com.alness.lifemaster.notes.entity.NotesEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.utils.DateTimeUtils;

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

        Specification<ExercisesEntity> specification = Specification.where(null);
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

    private Specification<ExercisesEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<NotesEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<ExercisesEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<ExercisesEntity> filterByDate(String date) {
        return (root, query, cb) -> cb.equal(root.<LocalDate>get("date"), DateTimeUtils.parseToLocalDate(date));

    }

}
