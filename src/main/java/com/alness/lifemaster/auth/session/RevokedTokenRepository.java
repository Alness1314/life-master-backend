package com.alness.lifemaster.auth.session;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedTokenEntity, String> {
    long deleteByExpiresAtBefore(Instant instant);
}
