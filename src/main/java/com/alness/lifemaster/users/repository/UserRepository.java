package com.alness.lifemaster.users.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.users.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity>{
    boolean existsByUsernameAndErasedFalse(String username);

    boolean existsByUsernameAndErasedFalseAndIdNot(String username, UUID id);
}
