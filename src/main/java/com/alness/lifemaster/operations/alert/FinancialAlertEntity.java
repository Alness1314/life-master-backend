package com.alness.lifemaster.operations.alert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "financial_alerts")
@Getter
@Setter
public class FinancialAlertEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "alert_type", nullable = false, length = 32)
    private String alertType;
    @Column(nullable = false, length = 16)
    private String severity;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(nullable = false, length = 1024)
    private String message;
    @Column(name = "reference_key", nullable = false, length = 256)
    private String referenceKey;
    @Column(nullable = false)
    private Boolean read;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @PrePersist
    void create() {
        createdAt = LocalDateTime.now(ZoneId.systemDefault());
        if (read == null)
            read = false;
    }
}
