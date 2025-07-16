package com.alness.lifemaster.modules.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.modules.dto.request.ModuleRequest;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.modules.repository.ModuleRepository;
import com.alness.lifemaster.modules.service.ModuleService;
import com.alness.lifemaster.modules.specification.ModuleSpecification;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.LoggerUtil;

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
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (RestExceptionHandler ex) {
            LoggerUtil.logError(ex);
            throw ex; // Re-lanzar excepciones ya gestionadas
        } catch (Exception ex) {
            LoggerUtil.logError(ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, Messages.ERROR_ENTITY_SAVE, ex);
        }

    }

    @Override
    public ModuleResponse updateModule(String id, ModuleRequest request) {
        ModuleEntity existingModule = moduleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        Messages.NOT_FOUND));

        try {
            // Actualizar campos básicos
            existingModule.setName(request.getName());
            existingModule.setRoute(request.getRoute());
            existingModule.setIconName(request.getIconName());
            existingModule.setLevel(request.getLevel());
            existingModule.setDescription(request.getDescription());
            existingModule.setIsParent(request.getIsParent());

            // Limpia perfiles actuales
            for (ProfileEntity profile : existingModule.getProfiles()) {
                profile.getModules().remove(existingModule);
            }
            existingModule.getProfiles().clear();

            // Asignar nuevos perfiles
            for (String profileId : request.getProfile()) {
                ProfileEntity profile = profileRepository.findById(UUID.fromString(profileId)).orElse(null);
                if (profile != null) {
                    existingModule.getProfiles().add(profile);
                    profile.getModules().add(existingModule);
                }
            }

            ModuleEntity updated = moduleRepository.save(existingModule);
            return mapperModule(updated);

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);
        } catch (RestExceptionHandler ex) {
            LoggerUtil.logError(ex);
            throw ex;
        } catch (Exception ex) {
            LoggerUtil.logError(ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ERROR_ENTITY_UPDATE, ex);
        }
    }

    @Override
    public ResponseServerDto deleteModule(String id) {
        ModuleEntity module = moduleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        Messages.NOT_FOUND));
        try {
            // Eliminar referencias cruzadas con perfiles
            for (ProfileEntity profile : module.getProfiles()) {
                profile.getModules().remove(module);
            }
            module.getProfiles().clear();
            moduleRepository.delete(module);
            return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
        } catch (RestExceptionHandler ex) {
            LoggerUtil.logError(ex);
            throw ex;
        } catch (Exception ex) {
            LoggerUtil.logError(ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.ERROR_ENTITY_DELETE, ex);
        }
    }

    @Override
    public ModuleResponse getModuleById(String id) {
        ModuleEntity module = moduleRepository.findById(UUID.fromString(id))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Messages.NOT_FOUND, id)));
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
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, parentId)));
        ModuleEntity child = moduleRepository.findById(UUID.fromString(childId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, childId)));

        if (Boolean.FALSE.equals(parent.getIsParent())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_412, HttpStatus.PRECONDITION_FAILED,
                    String.format(Messages.MODULE_NOT_PARENT, parentId));
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
