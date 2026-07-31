package com.alness.lifemaster.auth.session;

import java.time.Instant;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevokedTokenService {
    private final RevokedTokenRepository repository;

    @Transactional(readOnly = true)
    public boolean isRevoked(String tokenId) {
        return repository.existsById(tokenId);
    }

    @Transactional
    public void revoke(String tokenId, UUID userId, Instant expiresAt) {
        if (tokenId == null || expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            return;
        }

        RevokedTokenEntity revokedToken = new RevokedTokenEntity();
        revokedToken.setTokenId(tokenId);
        revokedToken.setUserId(userId);
        revokedToken.setExpiresAt(expiresAt);
        revokedToken.setRevokedAt(Instant.now());
        repository.save(revokedToken);
    }

    @Transactional
    @Scheduled(cron = "${app.sessions.cleanup-cron:0 40 0 * * *}",
            zone = "${app.sessions.zone:America/Mexico_City}")
    public void removeExpiredTokens() {
        long removed = repository.deleteByExpiresAtBefore(Instant.now());
        if (removed > 0) {
            log.info("Tokens revocados expirados eliminados: {}", removed);
        }
    }
}
