package com.alness.lifemaster.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.modules.dto.request.ModuleRequest;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.modules.repository.ModuleRepository;
import com.alness.lifemaster.modules.service.impl.ModuleServiceImpl;
import com.alness.lifemaster.permissions.entity.PermissionEntity;
import com.alness.lifemaster.permissions.repository.PermissionRepository;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
class ModulePermissionProvisioningTests {

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private GenericMapper mapper;
    @InjectMocks
    private ModuleServiceImpl moduleService;

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void creatingAModuleGrantsFullCrudAccessToEveryAssignedProfile() {
        UUID moduleId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        ModuleRequest request = ModuleRequest.builder()
                .name("Reportes")
                .route("/reports")
                .level("sidebar")
                .isParent(false)
                .profile(List.of(profileId.toString()))
                .build();
        ModuleEntity module = new ModuleEntity();
        module.setId(moduleId);
        module.setName(request.getName());
        module.setRoute(request.getRoute());
        ProfileEntity profile = new ProfileEntity();
        profile.setId(profileId);
        profile.setName("Usuario");

        when(mapper.map(request, ModuleEntity.class)).thenReturn(module);
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(moduleRepository.saveAndFlush(module)).thenReturn(module);
        when(permissionRepository.findAllById(any())).thenReturn(List.of());
        when(mapper.map(module, ModuleResponse.class)).thenReturn(new ModuleResponse());

        moduleService.createModule(request);

        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(permissionRepository).saveAll(captor.capture());
        List<PermissionEntity> saved = StreamSupport.stream(captor.getValue().spliterator(), false)
                .map(PermissionEntity.class::cast)
                .toList();
        assertThat(saved).singleElement().satisfies(permission -> {
            assertThat(permission.getProfile().getId()).isEqualTo(profileId);
            assertThat(permission.getModule().getId()).isEqualTo(moduleId);
            assertThat(permission.isCanCreate()).isTrue();
            assertThat(permission.isCanRead()).isTrue();
            assertThat(permission.isCanUpdate()).isTrue();
            assertThat(permission.isCanDelete()).isTrue();
        });
    }
}
