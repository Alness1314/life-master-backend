package com.alness.lifemaster.operations.audit;

import java.util.*;
import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    private final AuditEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String username, String method, String resource, String action,
            String module, String recordId, String detail, int status, String correlationId,
            String ip, String userAgent, long durationMs) {
        AuditEventEntity value = new AuditEventEntity();
        value.setUserId(userId); value.setUsername(username); value.setMethod(method);
        value.setResource(resource); value.setAction(action); value.setModule(module);
        value.setRecordId(recordId); value.setDetail(detail); value.setSuccessful(status < 400);
        value.setResponseStatus(status); value.setCorrelationId(correlationId);
        value.setIpAddress(ip); value.setUserAgent(userAgent); value.setDurationMs(durationMs);
        repository.save(value);
    }

    public void record(UUID userId, String username, String method, String resource, int status,
            String correlationId, String ip, long durationMs) {
        record(userId, username, method, resource,
                switch (method) {
                    case "POST" -> "CREAR";
                    case "PUT", "PATCH" -> "ACTUALIZAR";
                    case "DELETE" -> "ELIMINAR";
                    default -> "CONSULTAR";
                },
                "SISTEMA", null, method + " " + resource, status, correlationId, ip, null, durationMs);
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> find(UUID userId, int limit) {
        List<AuditEventEntity> values = userId == null
                ? repository.findAll(PageRequest.of(0, Math.min(limit, 200),
                        org.springframework.data.domain.Sort.by("createdAt").descending())).getContent()
                : repository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(limit, 200)));
        return values.stream().map(v -> new AuditEventResponse(v.getId(), v.getUserId(), v.getUsername(),
                v.getMethod(), v.getResource(), v.getAction(), v.getModule(), v.getRecordId(), v.getDetail(),
                v.getSuccessful(), v.getResponseStatus(), v.getCorrelationId(), v.getIpAddress(),
                v.getUserAgent(), v.getDurationMs(), v.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    public AuditEventPageResponse search(UUID userId, String username, String module, String action,
            String recordId, Boolean successful, LocalDate from, LocalDate to, int page, int size) {
        Specification<AuditEventEntity> specification = Specification
                .where(AuditEventSpecifications.userId(userId))
                .and(AuditEventSpecifications.text("username", username))
                .and(AuditEventSpecifications.text("module", module))
                .and(AuditEventSpecifications.text("action", action))
                .and(AuditEventSpecifications.text("recordId", recordId))
                .and(AuditEventSpecifications.successful(successful))
                .and(AuditEventSpecifications.from(from))
                .and(AuditEventSpecifications.to(to));
        Page<AuditEventEntity> result = repository.findAll(specification,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200),
                        Sort.by("createdAt").descending()));
        List<AuditEventResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new AuditEventPageResponse(content, result.getTotalElements(), result.getTotalPages(),
                result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public AuditEventResponse findOne(UUID id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Evento de auditoría no encontrado."));
    }

    private AuditEventResponse toResponse(AuditEventEntity v) {
        return new AuditEventResponse(v.getId(), v.getUserId(), v.getUsername(), v.getMethod(),
                v.getResource(), v.getAction(), v.getModule(), v.getRecordId(), v.getDetail(),
                v.getSuccessful(), v.getResponseStatus(), v.getCorrelationId(), v.getIpAddress(),
                v.getUserAgent(), v.getDurationMs(), v.getCreatedAt());
    }
}
