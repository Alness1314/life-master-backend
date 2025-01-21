package com.alness.lifemaster.income.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.income.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, UUID>, JpaSpecificationExecutor<IncomeEntity>{
    
}
