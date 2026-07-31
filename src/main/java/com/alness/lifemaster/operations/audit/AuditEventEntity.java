package com.alness.lifemaster.operations.audit;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
public class AuditEventEntity {
    @Id @GeneratedValue(generator = "uuid2") private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(length = 256) private String username;
    @Column(nullable = false, length = 8) private String method;
    @Column(nullable = false, length = 512) private String resource;
    @Column(nullable = false, length = 32) private String action;
    @Column(nullable = false, length = 64) private String module;
    @Column(name = "record_id", length = 64) private String recordId;
    @Column(length = 1024) private String detail;
    @Column(nullable = false) private Boolean successful;
    @Column(name = "response_status", nullable = false) private Integer responseStatus;
    @Column(name = "correlation_id", nullable = false, length = 64) private String correlationId;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "user_agent", length = 512) private String userAgent;
    @Column(name = "duration_ms", nullable = false) private Long durationMs;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @PrePersist void create() { createdAt = LocalDateTime.now(); }
}
