package com.alness.lifemaster.modules.specification;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

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

        Specification<ModuleEntity> specification = status();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<ModuleEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "profile" -> hasProfileId(entry.getValue());
                case "level" -> filterByLevel(entry.getValue());
                case "isParent" -> exactBoolean("isParent", entry.getValue());
                case "name", "route", "permissionKey", "iconName", "description" ->
                    containsIgnoreCase(entry.getKey(), entry.getValue());
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

    private Specification<ModuleEntity> filterById(String id) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<ModuleEntity> status() {
        return (root, query, cb) -> cb.equal(root.<Boolean>get("erased"), false);

    }

    private Specification<ModuleEntity> filterByLevel(String level) {
        return (root, query, cb) -> cb.equal(
                cb.lower(root.<String>get("level")), level.toLowerCase(Locale.ROOT));
    }

    private Specification<ModuleEntity> exactBoolean(String field, String value) {
        return (root, query, cb) -> cb.equal(root.<Boolean>get(field), Boolean.valueOf(value));

    }

    public Specification<ModuleEntity> hasProfileId(String profileId) {
        return (root, query, criteriaBuilder) -> {
            Join<ModuleEntity, ProfileEntity> profileJoin = root.join("profiles");
            return criteriaBuilder.equal(profileJoin.<UUID>get("id"), UUID.fromString(profileId));
        };
    }

}
