package com.alness.lifemaster.vault.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.vault.entity.VaultEntity;

public interface VaultRepository extends JpaRepository<VaultEntity, UUID>, JpaSpecificationExecutor<VaultEntity>{
    
}
