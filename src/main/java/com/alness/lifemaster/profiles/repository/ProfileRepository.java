package com.alness.lifemaster.profiles.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.alness.lifemaster.profiles.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID>, JpaSpecificationExecutor<ProfileEntity>{
    public Optional<ProfileEntity> findByName(String name);

    @Query("SELECT p FROM ProfileEntity p JOIN FETCH p.modules WHERE p.id = :profileId")
    public Optional<ProfileEntity> findByIdWithModules(UUID profileId);
}
