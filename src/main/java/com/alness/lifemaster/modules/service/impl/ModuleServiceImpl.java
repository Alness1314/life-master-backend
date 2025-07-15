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

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
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

                if (profile != null) {
                    newModule.getProfiles().add(profile);
                    profile.getModules().add(newModule); // Actualiza el otro lado de la relación
                }
            }
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
                .description(module.getDescription())
                .level(module.getLevel())
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
    public ResponseServerDto multiSave(List<ModuleRequest> modules) {
        List<Map<String, Object>> response = new ArrayList<>();
        modules.forEach(item -> {
            ModuleResponse resp = createModule(item);

            Boolean status = (resp != null);

            String name = Boolean.TRUE.equals((status)) ? resp.getName() : item.getName(); 

            response.add(Map.of(Filters.KEY_MODULE, name, Filters.KEY_STATUS, status));
        });
        return new ResponseServerDto("Modulos creados", HttpStatus.ACCEPTED, true, Map.of(Filters.KEY_DATA, response));
    }

}
