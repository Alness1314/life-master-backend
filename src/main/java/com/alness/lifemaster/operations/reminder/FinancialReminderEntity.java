package com.alness.lifemaster.operations.reminder;

import java.time.LocalDateTime;
import java.util.UUID;
import com.alness.lifemaster.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "financial_reminders")
@Getter
@Setter
public class FinancialReminderEntity {
    @Id @GeneratedValue(generator = "uuid2") private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private UserEntity user;
    @Column(nullable = false, length = 256) private String title;
    @Column(nullable = false, length = 1024) private String message;
    @Column(name = "scheduled_at", nullable = false) private LocalDateTime scheduledAt;
    @Column(nullable = false) private Boolean delivered;
    @Column(nullable = false) private Boolean cancelled;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @PrePersist void create() {
        createdAt = LocalDateTime.now();
        if (delivered == null) delivered = false;
        if (cancelled == null) cancelled = false;
    }
}
