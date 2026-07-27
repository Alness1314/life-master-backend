package com.alness.lifemaster.operations.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEventResponse(UUID id, UUID userId, String username, String method, String resource,
                Integer responseStatus, String correlationId, String ipAddress, Long durationMs,
                LocalDateTime createdAt) {
}
