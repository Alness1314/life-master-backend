package com.alness.lifemaster.debts.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.debts.entity.DebtsEntity;

public interface DebtsRespository extends JpaRepository<DebtsEntity, UUID>, JpaSpecificationExecutor<DebtsEntity> {
    List<DebtsEntity> findAllByUserIdAndErasedFalse(UUID userId);
    Optional<DebtsEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
}
