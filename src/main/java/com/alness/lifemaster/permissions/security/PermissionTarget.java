package com.alness.lifemaster.permissions.security;

import com.alness.lifemaster.permissions.PermissionAction;

public record PermissionTarget(String permissionKey, PermissionAction action) {
}
