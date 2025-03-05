package com.alness.lifemaster.modules.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.profiles.entity.ProfileEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ModuleSpecification implements Specification<ModuleEntity> {

    @SuppressWarnings("null")
    @Override
    @Nullable
    public Predicate toPredicate(Root<ModuleEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }

    public Specification<ModuleEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<ModuleEntity> specification = getDefaultSpecification();
        for (Map.Entry<String, String> entry : params.entrySet()) {

            switch (entry.getKey()) {
                case "id":
                    specification = specification.and(this.filterById(entry.getValue()));
                    break;
                case "profile":
                    specification = specification.and(this.hasProfileId(entry.getValue()));
                    break;
                case "level":
                    specification = specification.and(this.filterByLevel(entry.getValue()));
                    break;
                default:
                    break;
            }
        }
        return specification;
    }

    private Specification<ModuleEntity> filterById(String id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.<String>get("id"), id);
    }

    private Specification<ModuleEntity> getDefaultSpecification() {
        return Specification.where(this.status());
    }

    private Specification<ModuleEntity> status() {
        return (root, query, cb) -> cb.equal(root.<Boolean>get("erased"), false);

    }

    private Specification<ModuleEntity> filterByLevel(String level) {
        return (root, query, cb) -> cb.equal(root.<String>get("level"), level);

    }

    public Specification<ModuleEntity> hasProfileId(String profileId) {
        return (root, query, criteriaBuilder) -> {
            Join<ModuleEntity, ProfileEntity> profileJoin = root.join("profiles");
            return criteriaBuilder.equal(profileJoin.<UUID>get("id"), UUID.fromString(profileId));
        };
    }

}
