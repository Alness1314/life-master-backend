package com.alness.lifemaster.users.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.alness.lifemaster.users.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity>{
    boolean existsByUsernameAndErasedFalse(String username);

    boolean existsByUsernameAndErasedFalseAndIdNot(String username, UUID id);

    @Query("select user.id from UserEntity user where user.erased = false")
    List<UUID> findAllActiveIds();
}
