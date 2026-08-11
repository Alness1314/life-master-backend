package com.alness.lifemaster.nutrition.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.nutrition.entity.NutritionEntity;

public interface NutritionRepository extends JpaRepository<NutritionEntity, UUID>, JpaSpecificationExecutor<NutritionEntity>{
    Optional<NutritionEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    List<NutritionEntity> findAllByUserIdAndDateTimeConsumptionBetweenAndErasedFalseOrderByDateTimeConsumptionAsc(
            UUID userId, LocalDateTime from, LocalDateTime to);
}
