package com.alness.lifemaster.assistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alness.lifemaster.assistance.entity.AssistanceEntity;

public interface AssistanceRepository extends JpaRepository<AssistanceEntity, UUID>{
    
}
