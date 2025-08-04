package com.alness.lifemaster.assistance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "assistance")
public class AssistanceEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "work_date", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDate workDate;

    @Column(name = "hora_entrada", nullable = true, updatable = true, columnDefinition = "time without time zone")
    private LocalTime timeEntry;

    @Column(name = "hora_salida", nullable = true, updatable = true, columnDefinition = "time without time zone")
    private LocalTime departureTime;

    @Column(name = "on_time", nullable = true, columnDefinition = "boolean")
    private Boolean onTime;

    @Column(nullable = true, columnDefinition = "boolean")
    private Boolean retard;

    @Column(name = "justified_absence", nullable = true, columnDefinition = "boolean")
    private Boolean justifiedAbsence;

    @Column(name = "unjustified_absence", nullable = true, columnDefinition = "boolean")
    private Boolean unjustifiedAbsence;

    @Column(name = "create_at", nullable = false, updatable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false, updatable = true, columnDefinition = "timestamp without time zone")
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @PrePersist
    private void init(){
        setCreateAt(LocalDateTime.now());
        setUpdateAt(LocalDateTime.now());
    }

    @PreUpdate
    private void preUpdate(){
        setUpdateAt(LocalDateTime.now());
    }
}
