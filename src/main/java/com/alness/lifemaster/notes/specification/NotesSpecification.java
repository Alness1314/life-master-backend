package com.alness.lifemaster.notes.specification;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.notes.entity.NotesEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class NotesSpecification implements Specification<NotesEntity>{

    @SuppressWarnings("null")
    @Override
    @Nullable
    public Predicate toPredicate(Root<NotesEntity> root, @Nullable CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
      return null;
    }
    
    public Specification<NotesEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<NotesEntity> specification = Specification.where(null);
        for (Entry<String, String> entry : params.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                    specification = specification.and(this.filterById(entry.getValue()));
                    break;
                case "title":
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

    private Specification<NotesEntity> filterByUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            Join<NotesEntity, UserEntity> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(userId));
        };
    }

    private Specification<NotesEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<NotesEntity> filterBySource(String title) {
        return (root, query, cb) -> cb.equal(root.<String>get("title"), title);

    }
}
