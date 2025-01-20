package com.alness.lifemaster.assistance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

    @Column(nullable = false, updatable = false, columnDefinition = "date")
    private LocalDate fecha;

    @Column(nullable = true, updatable = false, columnDefinition = "time without time zone")
    private LocalTime horaEntrada;

    @Column(nullable = true, updatable = false, columnDefinition = "time without time zone")
    private LocalTime horaSalida;

    @Column(nullable = true, columnDefinition = "boolean")
    private Boolean retardo;

    @Column(nullable = true, columnDefinition = "boolean")
    private Boolean faltaJust;

    @Column(nullable = true, columnDefinition = "boolean")
    private Boolean faltaInjust;

    @Column(nullable = false, updatable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime createAt;

    @Column(nullable = false, updatable = true, columnDefinition = "timestamp without time zone")
    private LocalDateTime updateAt;

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean erased;

    @PrePersist
    private void init(){
        setCreateAt(LocalDateTime.now());
        setErased(false);
    }
}
