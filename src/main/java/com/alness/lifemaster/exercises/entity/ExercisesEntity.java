package com.alness.lifemaster.exercises.entity;

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
import lombok.Data;

@Entity
@Table(name = "exercises")
@Data
public class ExercisesEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "notes", nullable = true, columnDefinition = "text")
    private String notes;

     @Column(name = "training_date", nullable = false, columnDefinition = "date")
    private LocalDate trainingDate;

     @Column(name = "start_time", nullable = true, columnDefinition = "time without time zone")
    private LocalTime startTime;

     @Column(name = "end_time", nullable = true, columnDefinition = "time without time zone")
    private LocalTime endTime;

     @Column(name = "activity_type", nullable = true, columnDefinition = "character varying(128)")
    private String activityType;

     @Column(name = "duration_minutes", nullable = true, columnDefinition = "bigint")
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "create_at", nullable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false, updatable = true, columnDefinition = "timestamp without time zone")
    private LocalDateTime updateAt;

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean erased;

    @PrePersist()
    public void init() {
        setCreateAt(LocalDateTime.now());
        setUpdateAt(LocalDateTime.now());
        setErased(false);
    }

    @PreUpdate
    private void preUpdate() {
        setUpdateAt(LocalDateTime.now());
    }
}
