package com.alness.lifemaster.users.dto;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUser extends User {
    private final UUID userId;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
            UUID userId) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // Verifica si son la misma instancia
        if (!(o instanceof CustomUser))
            return false; // Verifica si es del mismo tipo
        if (!super.equals(o))
            return false; // Usa la lógica de igualdad de la clase padre

        CustomUser that = (CustomUser) o;
        return Objects.equals(userId, that.userId); // Compara la propiedad userId
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId); // Incluye el hashCode de la clase padre y userId
    }
}
