package com.alness.lifemaster.expenses.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.expenses.entity.ExpensesEntity;

public interface ExpensesRepository extends JpaRepository<ExpensesEntity, UUID>, JpaSpecificationExecutor<ExpensesEntity>{
    
}
