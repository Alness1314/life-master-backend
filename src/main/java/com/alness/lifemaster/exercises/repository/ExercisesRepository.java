package com.alness.lifemaster.exercises.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.exercises.entity.ExercisesEntity;

public interface ExercisesRepository
        extends JpaRepository<ExercisesEntity, UUID>, JpaSpecificationExecutor<ExercisesEntity> {
    List<ExercisesEntity> findAllByUserIdAndTrainingDateBetweenAndErasedFalseOrderByTrainingDateAsc(
            UUID userId, LocalDate from, LocalDate to);
}
