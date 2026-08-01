package com.alness.lifemaster.permissions.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.modules.repository.ModuleRepository;
import com.alness.lifemaster.permissions.dto.PermissionRequest;
import com.alness.lifemaster.permissions.dto.PermissionResponse;
import com.alness.lifemaster.permissions.dto.PermissionUpdateRequest;
import com.alness.lifemaster.permissions.entity.PermissionEntity;
import com.alness.lifemaster.permissions.entity.PermissionId;
import com.alness.lifemaster.permissions.repository.PermissionRepository;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final ProfileRepository profileRepository;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> find(UUID profileId, UUID moduleId) {
        return permissionRepository.findForAdministration(profileId, moduleId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PermissionResponse create(PermissionRequest request) {
        PermissionId id = new PermissionId(request.getProfileId(), request.getModuleId());
        if (permissionRepository.existsById(id)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    "El permiso ya está registrado para el perfil y módulo indicados.");
        }
        ProfileEntity profile = activeProfile(request.getProfileId());
        ModuleEntity module = activeModule(request.getModuleId());
        requireAssignment(profile, module);

        PermissionEntity permission = new PermissionEntity();
        permission.setId(id);
        permission.setProfile(profile);
        permission.setModule(module);
        apply(permission, request.isCanCreate(), request.isCanRead(),
                request.isCanUpdate(), request.isCanDelete());
        return toResponse(permissionRepository.save(permission));
    }

    public PermissionResponse update(UUID profileId, UUID moduleId, PermissionUpdateRequest request) {
        PermissionEntity permission = findOne(profileId, moduleId);
        requireAssignment(permission.getProfile(), permission.getModule());
        apply(permission, request.isCanCreate(), request.isCanRead(),
                request.isCanUpdate(), request.isCanDelete());
        return toResponse(permissionRepository.save(permission));
    }

    public ResponseServerDto delete(UUID profileId, UUID moduleId) {
        PermissionEntity permission = findOne(profileId, moduleId);
        permissionRepository.delete(permission);
        return new ResponseServerDto("Permiso eliminado.", HttpStatus.ACCEPTED, true);
    }

    private PermissionEntity findOne(UUID profileId, UUID moduleId) {
        return permissionRepository.findById(new PermissionId(profileId, moduleId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "No se encontró el permiso solicitado."));
    }

    private ProfileEntity activeProfile(UUID profileId) {
        return profileRepository.findByIdWithModules(profileId)
                .filter(profile -> !Boolean.TRUE.equals(profile.getErased()))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "No se encontró el perfil indicado."));
    }

    private ModuleEntity activeModule(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .filter(module -> !Boolean.TRUE.equals(module.getErased()))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "No se encontró el módulo indicado."));
    }

    private void requireAssignment(ProfileEntity profile, ModuleEntity module) {
        if (!profile.getModules().contains(module)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_412, HttpStatus.PRECONDITION_FAILED,
                    "Primero debes asignar el módulo al perfil.");
        }
    }

    private void apply(PermissionEntity permission, boolean canCreate, boolean canRead,
            boolean canUpdate, boolean canDelete) {
        permission.setCanCreate(canCreate);
        permission.setCanRead(canRead);
        permission.setCanUpdate(canUpdate);
        permission.setCanDelete(canDelete);
    }

    private PermissionResponse toResponse(PermissionEntity permission) {
        return PermissionResponse.builder()
                .profileId(permission.getProfile().getId())
                .profileName(permission.getProfile().getName())
                .moduleId(permission.getModule().getId())
                .moduleName(permission.getModule().getName())
                .moduleRoute(permission.getModule().getRoute())
                .permissionKey(permission.getModule().getPermissionKey())
                .canCreate(permission.isCanCreate())
                .canRead(permission.isCanRead())
                .canUpdate(permission.isCanUpdate())
                .canDelete(permission.isCanDelete())
                .build();
    }
}
