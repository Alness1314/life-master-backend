package com.alness.lifemaster.permissions.service;

import java.util.UUID;

public record EffectivePermission(
        UUID moduleId,
        boolean canCreate,
        boolean canRead,
        boolean canUpdate,
        boolean canDelete) {

    public boolean allows(com.alness.lifemaster.permissions.PermissionAction action) {
        return switch (action) {
            case CREATE -> canCreate;
            case READ -> canRead;
            case UPDATE -> canUpdate;
            case DELETE -> canDelete;
        };
    }

    public EffectivePermission merge(EffectivePermission other) {
        return new EffectivePermission(
                moduleId,
                canCreate || other.canCreate,
                canRead || other.canRead,
                canUpdate || other.canUpdate,
                canDelete || other.canDelete);
    }
}
