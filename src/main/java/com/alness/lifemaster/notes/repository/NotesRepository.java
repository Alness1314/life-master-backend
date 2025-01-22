package com.alness.lifemaster.notes.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.notes.entity.NotesEntity;

public interface NotesRepository extends JpaRepository<NotesEntity, UUID>, JpaSpecificationExecutor<NotesEntity>{
    
}
