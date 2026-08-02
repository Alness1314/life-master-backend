package com.alness.lifemaster.categories.specification;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import static com.alness.lifemaster.common.specification.FilterSpecifications.containsIgnoreCase;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class CategorySpecification implements Specification<CategoryEntity> {

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<CategoryEntity> arg0, CriteriaQuery<?> arg1, CriteriaBuilder arg2) {
        return null;
    }

    public Specification<CategoryEntity> getSpecificationByFilters(Map<String, String> params) {

        Specification<CategoryEntity> specification = notErased();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Specification<CategoryEntity> currentFilter = switch (entry.getKey()) {
                case "id" -> filterById(entry.getValue());
                case "name", "description" -> containsIgnoreCase(entry.getKey(), entry.getValue());
                default -> null;
            };
            if (currentFilter != null) {
                specification = specification.and(currentFilter);
            }
        }
        return specification;
    }

    private Specification<CategoryEntity> filterById(String id) {
        return (root, query, cb) -> cb.equal(root.<UUID>get("id"), UUID.fromString(id));
    }

    private Specification<CategoryEntity> notErased() {
        return (root, query, cb) -> cb.isFalse(root.get("erased"));
    }

}
