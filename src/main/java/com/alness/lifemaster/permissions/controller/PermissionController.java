package com.alness.lifemaster.permissions.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.permissions.dto.PermissionRequest;
import com.alness.lifemaster.permissions.dto.PermissionResponse;
import com.alness.lifemaster.permissions.dto.PermissionUpdateRequest;
import com.alness.lifemaster.permissions.service.PermissionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/permissions")
@Tag(name = "Permissions", description = "Permisos CRUD por perfil y módulo.")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping
    public List<PermissionResponse> find(
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) UUID moduleId) {
        return permissionService.find(profileId, moduleId);
    }

    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @PutMapping("/profiles/{profileId}/modules/{moduleId}")
    public ResponseEntity<PermissionResponse> update(
            @PathVariable UUID profileId,
            @PathVariable UUID moduleId,
            @RequestBody PermissionUpdateRequest request) {
        return ResponseEntity.accepted().body(permissionService.update(profileId, moduleId, request));
    }

    @DeleteMapping("/profiles/{profileId}/modules/{moduleId}")
    public ResponseEntity<ResponseServerDto> delete(
            @PathVariable UUID profileId,
            @PathVariable UUID moduleId) {
        return ResponseEntity.accepted().body(permissionService.delete(profileId, moduleId));
    }
}
