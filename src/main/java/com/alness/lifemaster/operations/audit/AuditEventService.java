package com.alness.lifemaster.operations.audit;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    private final AuditEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String username, String method, String resource, int status,
            String correlationId, String ip, long durationMs) {
        AuditEventEntity value = new AuditEventEntity();
        value.setUserId(userId); value.setUsername(username); value.setMethod(method);
        value.setResource(resource); value.setResponseStatus(status); value.setCorrelationId(correlationId);
        value.setIpAddress(ip); value.setDurationMs(durationMs);
        repository.save(value);
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> find(UUID userId, int limit) {
        List<AuditEventEntity> values = userId == null
                ? repository.findAll(PageRequest.of(0, Math.min(limit, 200),
                        org.springframework.data.domain.Sort.by("createdAt").descending())).getContent()
                : repository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(limit, 200)));
        return values.stream().map(v -> new AuditEventResponse(v.getId(), v.getUserId(), v.getUsername(),
                v.getMethod(), v.getResource(), v.getResponseStatus(), v.getCorrelationId(), v.getIpAddress(),
                v.getDurationMs(), v.getCreatedAt())).toList();
    }
}
