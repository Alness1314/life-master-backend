package com.alness.lifemaster.auth.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokedTokenServiceTests {
    @Mock
    private RevokedTokenRepository repository;

    @InjectMocks
    private RevokedTokenService service;

    @Test
    void persistsAnUnexpiredRevokedToken() {
        String tokenId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        service.revoke(tokenId, userId, expiresAt);

        verify(repository).save(argThat(value ->
                tokenId.equals(value.getTokenId())
                        && userId.equals(value.getUserId())
                        && expiresAt.equals(value.getExpiresAt())
                        && value.getRevokedAt() != null));
    }

    @Test
    void ignoresAnAlreadyExpiredToken() {
        service.revoke(UUID.randomUUID().toString(), UUID.randomUUID(), Instant.now().minusSeconds(1));

        verify(repository, never()).save(any());
    }

    @Test
    void reportsPersistedRevocation() {
        String tokenId = UUID.randomUUID().toString();
        when(repository.existsById(tokenId)).thenReturn(true);

        Assertions.assertTrue(service.isRevoked(tokenId));
    }
}
