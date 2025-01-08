package com.alness.lifemaster.profiles.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter @Setter
public class ProfileEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "character varying(64)")
    private String name;

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean erased;

    @Column(nullable = false, updatable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime created;

    @Column(nullable = false, updatable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime updated;

    @PrePersist
    public void prePersist(){
        setErased(false);
        setCreated(LocalDateTime.now());
        setUpdated(LocalDateTime.now());
    }
}
