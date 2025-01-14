package com.alness.lifemaster.modules.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alness.lifemaster.modules.entity.ModuleEntity;

public interface ModuleRepository extends JpaRepository<ModuleEntity, UUID>{

    
} 