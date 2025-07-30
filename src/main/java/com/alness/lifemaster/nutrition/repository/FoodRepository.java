package com.alness.lifemaster.nutrition.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.nutrition.entity.FoodEntity;

public interface FoodRepository extends JpaRepository<FoodEntity, UUID>, JpaSpecificationExecutor<FoodEntity>{
    
}
