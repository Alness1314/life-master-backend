package com.alness.lifemaster.modules.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "modules")
@Getter
@Setter
public class ModuleEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "route", nullable = false)
    private String route;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "erased", nullable = false)
    private Boolean erased;

    @Column(name = "is_parent", nullable = false)
    private Boolean isParent;

    @Column(name = "level")
    private String level;

    @Column(name = "description")
    private String description;

    // Relación de auto-referencia para representar la jerarquía de módulos (padre e
    // hijos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference // Marca esta relación como la inversa (evita el bucle)
    private ModuleEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // Marca esta relación como la administrada
    private Set<ModuleEntity> children = new HashSet<>();

    @ManyToMany(mappedBy = "modules", fetch = FetchType.LAZY)
    private Set<ProfileEntity> profiles = new HashSet<>();

    @PrePersist
    private void init() {
        setErased(false);
    }

    // Métodos de conveniencia
    public void addChild(ModuleEntity child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(ModuleEntity child) {
        this.children.remove(child);
        child.setParent(null);
    }

    @Override
    public String toString() {
        return "ModuleEntity [id=" + id + ", name=" + name + ", route=" + route + ", erased=" + erased + ", isParent="
                + isParent + ", profiles=" + profiles + "]";
    }

}
