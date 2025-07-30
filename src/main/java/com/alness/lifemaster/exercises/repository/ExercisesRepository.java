package com.alness.lifemaster.exercises.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.exercises.entity.ExercisesEntity;

public interface ExercisesRepository
        extends JpaRepository<ExercisesEntity, UUID>, JpaSpecificationExecutor<ExercisesEntity> {

}
