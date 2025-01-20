package com.alness.lifemaster.profiles.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.alness.lifemaster.modules.entity.ModuleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "profile_modules", 
        joinColumns = @JoinColumn(name = "profile_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "module_id", referencedColumnName = "id"))
    private Set<ModuleEntity> modules = new HashSet<>();

    @PrePersist
    public void prePersist() {
        setErased(false);
        setCreated(LocalDateTime.now());
        setUpdated(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "ProfileEntity [id=" + id + ", name=" + name + ", erased=" + erased + "]";
    }

}
