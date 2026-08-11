package com.alness.lifemaster.assistance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.assistance.entity.AssistanceEntity;

public interface AssistanceRepository extends JpaRepository<AssistanceEntity, UUID>, JpaSpecificationExecutor<AssistanceEntity>{
    List<AssistanceEntity> findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(
            UUID userId, LocalDate from, LocalDate to);
}
