package com.alness.lifemaster.profiles.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.profiles.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID>, JpaSpecificationExecutor<ProfileEntity>{
    public Optional<ProfileEntity> findByName(String name);
}
