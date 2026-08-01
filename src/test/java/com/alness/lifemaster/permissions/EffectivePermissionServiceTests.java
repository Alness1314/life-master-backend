package com.alness.lifemaster.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.permissions.entity.PermissionEntity;
import com.alness.lifemaster.permissions.entity.PermissionId;
import com.alness.lifemaster.permissions.repository.PermissionRepository;
import com.alness.lifemaster.permissions.service.EffectivePermissionService;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;

class EffectivePermissionServiceTests {
    private UserRepository users;
    private PermissionRepository permissions;
    private EffectivePermissionService service;
    private UserEntity user;
    private ProfileEntity firstProfile;
    private ProfileEntity secondProfile;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        permissions = mock(PermissionRepository.class);
        service = new EffectivePermissionService(users, permissions);

        firstProfile = profile();
        secondProfile = profile();
        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setErased(false);
        user.setProfiles(List.of(firstProfile, secondProfile));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void grantsAnActionWhenAnyActiveProfileAllowsIt() {
        ModuleEntity categories = module("categories");
        when(permissions.findEffectiveByProfileIdsAndPermissionKey(
                List.of(firstProfile.getId(), secondProfile.getId()), "categories"))
                .thenReturn(List.of(
                        permission(firstProfile, categories, false, true, false, false),
                        permission(secondProfile, categories, true, true, false, false)));

        assertThat(service.hasPermission(user.getId(), "/CATEGORIES/", PermissionAction.CREATE)).isTrue();
        assertThat(service.hasPermission(user.getId(), "categories", PermissionAction.DELETE)).isFalse();
    }

    @Test
    void deniesInactiveUsersBeforeLookingUpPermissionRows() {
        user.setErased(true);

        assertThat(service.hasPermission(user.getId(), "categories", PermissionAction.READ)).isFalse();
    }

    private ProfileEntity profile() {
        ProfileEntity profile = new ProfileEntity();
        profile.setId(UUID.randomUUID());
        profile.setErased(false);
        return profile;
    }

    private ModuleEntity module(String permissionKey) {
        ModuleEntity module = new ModuleEntity();
        module.setId(UUID.randomUUID());
        module.setPermissionKey(permissionKey);
        module.setErased(false);
        return module;
    }

    private PermissionEntity permission(ProfileEntity profile, ModuleEntity module,
            boolean create, boolean read, boolean update, boolean delete) {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(new PermissionId(profile.getId(), module.getId()));
        permission.setProfile(profile);
        permission.setModule(module);
        permission.setCanCreate(create);
        permission.setCanRead(read);
        permission.setCanUpdate(update);
        permission.setCanDelete(delete);
        return permission;
    }
}
