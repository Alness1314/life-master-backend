package com.alness.lifemaster.permissions.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.permissions.PermissionAction;
import com.alness.lifemaster.permissions.entity.PermissionEntity;
import com.alness.lifemaster.permissions.repository.PermissionRepository;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EffectivePermissionService {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public boolean hasPermission(UUID userId, String permissionKey, PermissionAction action) {
        UserEntity user = activeUser(userId);
        List<UUID> profileIds = activeProfileIds(user);
        if (profileIds.isEmpty() || permissionKey == null || permissionKey.isBlank()) {
            return false;
        }
        return permissionRepository
                .findEffectiveByProfileIdsAndPermissionKey(profileIds, normalize(permissionKey))
                .stream()
                .anyMatch(permission -> allows(permission, action));
    }

    public Map<UUID, EffectivePermission> findEffectivePermissions(UserEntity user) {
        List<UUID> profileIds = activeProfileIds(user);
        if (profileIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, EffectivePermission> result = new LinkedHashMap<>();
        for (PermissionEntity permission : permissionRepository.findEffectiveByProfileIds(profileIds)) {
            EffectivePermission current = toEffective(permission);
            result.merge(current.moduleId(), current, EffectivePermission::merge);
        }
        return result;
    }

    private UserEntity activeUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getErased()))
                .orElse(null);
    }

    private List<UUID> activeProfileIds(UserEntity user) {
        if (user == null || user.getProfiles() == null) {
            return List.of();
        }
        return user.getProfiles().stream()
                .filter(profile -> !Boolean.TRUE.equals(profile.getErased()))
                .map(ProfileEntity::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private EffectivePermission toEffective(PermissionEntity permission) {
        return new EffectivePermission(
                permission.getModule().getId(),
                permission.isCanCreate(),
                permission.isCanRead(),
                permission.isCanUpdate(),
                permission.isCanDelete());
    }

    private boolean allows(PermissionEntity permission, PermissionAction action) {
        return switch (action) {
            case CREATE -> permission.isCanCreate();
            case READ -> permission.isCanRead();
            case UPDATE -> permission.isCanUpdate();
            case DELETE -> permission.isCanDelete();
        };
    }

    private String normalize(String value) {
        return value.trim().replaceAll("^/+|/+$", "").toLowerCase(java.util.Locale.ROOT);
    }
}
