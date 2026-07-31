package com.alness.lifemaster.operations.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEventResponse(UUID id, UUID userId, String username, String method, String resource,
                String action, String module, String recordId, String detail, Boolean successful,
                Integer responseStatus, String correlationId, String ipAddress, String userAgent,
                Long durationMs, LocalDateTime createdAt) {
}
