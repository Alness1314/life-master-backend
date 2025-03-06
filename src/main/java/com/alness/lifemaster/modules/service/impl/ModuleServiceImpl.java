package com.alness.lifemaster.modules.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.alness.lifemaster.app.dto.ResponseServer;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.modules.dto.request.ModuleRequest;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.modules.repository.ModuleRepository;
import com.alness.lifemaster.modules.service.ModuleService;
import com.alness.lifemaster.modules.specification.ModuleSpecification;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModuleServiceImpl implements ModuleService {
    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ProfileRepository profileRepository;

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public ModuleResponse createModule(ModuleRequest module) {
        ModuleEntity newModule = modelMapper.map(module, ModuleEntity.class);
        try {
            for (String profileName : module.getProfile()) {
                ProfileEntity profile = profileRepository.findById(UUID.fromString(profileName)).orElse(null);
                log.info("profile: {}", profile);

                if (profile != null) {
                    newModule.getProfiles().add(profile);
                    profile.getModules().add(newModule); // Actualiza el otro lado de la relación
                }
            }
            log.info("module to save {}", newModule);
            newModule = moduleRepository.save(newModule);
            return mapperModule(newModule);
        } catch (Exception e) {
            log.error("error to save", e);
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "error to save module");
        }

    }

    @Override
    public ModuleResponse updateModule(String id, ModuleRequest moduleDetails) {
        throw new UnsupportedOperationException("Unimplemented method 'updateModule'");
    }

    @Override
    public ResponseDto deleteModule(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteModule'");
    }

    @Override
    public ModuleResponse getModuleById(String id) {
        ModuleEntity module = moduleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found"));
        return mapperModule(module);
    }

    @Override
    public List<ModuleResponse> getAllModules() {
        return moduleRepository.findAll().stream()
                .filter(module -> module.getParent() == null) // Excluir los módulos asignados a un padre
                .map(this::convertToDto).toList();
    }

    @Override
    public ModuleResponse assignChildToParent(String parentId, String childId) {
        ModuleEntity parent = moduleRepository.findById(UUID.fromString(parentId))
                .orElseThrow(() -> new IllegalArgumentException("Parent module not found with id: " + parentId));
        ModuleEntity child = moduleRepository.findById(UUID.fromString(childId))
                .orElseThrow(() -> new IllegalArgumentException("Child module not found with id: " + childId));

        if (Boolean.FALSE.equals(parent.getIsParent())) {
            throw new IllegalArgumentException("Module with id: " + parentId + " is not marked as a parent");
        }

        child.setParent(parent);
        parent.getChildren().add(child);

        moduleRepository.save(child);
        return convertToDto(moduleRepository.save(parent));
    }

    public ModuleResponse mapperModule(ModuleEntity module) {
        return modelMapper.map(module, ModuleResponse.class);
    }

    private ModuleResponse convertToDto(ModuleEntity module) {
        return ModuleResponse.builder()
                .id(module.getId())
                .name(module.getName())
                .route(module.getRoute())
                .iconName(module.getIconName())
                .isParent(module.getIsParent())
                .erased(module.getErased())
                .children(module.getChildren().stream().map(this::convertToDto).toList())
                .build();
    }

    @Override
    public List<ModuleDto> find(Map<String, String> params) {
        return moduleRepository.findAll(filterWithParameters(params))
                .stream().map(this::mapperModules)
                .toList();
    }

    public ModuleDto mapperModules(ModuleEntity module) {
        return modelMapper.map(module, ModuleDto.class);
    }

    public Specification<ModuleEntity> filterWithParameters(Map<String, String> parameters) {
        return new ModuleSpecification().getSpecificationByFilters(parameters);
    }

    @Override
    public ResponseServer multiSave(List<ModuleRequest> modules) {
        List<Map<String, Object>> response = new ArrayList<>();
        modules.forEach(item -> {
            ModuleResponse resp = createModule(item);
            if (resp != null) {
                response.add(Map.of("module", resp.getName(), "status", true));
            } else {
                response.add(Map.of("module", item.getName(), "status", false));
            }
        });
        return new ResponseServer("Modulos creados", Map.of("data", response), true, HttpStatus.ACCEPTED);
    }

}
