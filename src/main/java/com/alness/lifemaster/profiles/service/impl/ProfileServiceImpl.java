package com.alness.lifemaster.profiles.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.profiles.dto.request.ProfileRequest;
import com.alness.lifemaster.profiles.dto.response.ProfileResponse;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.profiles.service.ProfileService;
import com.alness.lifemaster.profiles.specification.ProfileSpecification;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.LoggerUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final GenericMapper mapper;

    @Override
    public ProfileResponse save(ProfileRequest request) {
        ProfileEntity newProfile = mapper.map(request, ProfileEntity.class);
        try {
            newProfile = profileRepository.save(newProfile);
            return mapperDto(newProfile);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ProfileResponse findOne(String id) {
        ProfileEntity profile = profileRepository.findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapperDto(profile);
    }

    @Override
    public List<ProfileResponse> find(Map<String, String> params) {
        return profileRepository.findAll(filterWithParameters(params))
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public ProfileResponse update(String id, ProfileRequest request) {
        ProfileEntity existingProfile = profileRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        // Mapear datos del request al perfil existente
        mapper.map(request, existingProfile);

        try {
            existingProfile = profileRepository.save(existingProfile);
            return mapperDto(existingProfile);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ResponseServerDto delete(String id) {
        ProfileEntity profile = profileRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        try {
            profileRepository.delete(profile);
            return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    String.format(Messages.ERROR_ENTITY_DELETE, e.getMessage()));
        }
    }

    private ProfileResponse mapperDto(ProfileEntity source) {
        return mapper.map(source, ProfileResponse.class);
    }

    public Specification<ProfileEntity> filterWithParameters(Map<String, String> parameters) {
        return new ProfileSpecification().getSpecificationByFilters(parameters);
    }

    @Override
    public ProfileResponse findByName(String name) {
        ProfileEntity profile = profileRepository.findOne(filterWithParameters(Map.of(Filters.KEY_NAME, name)))
                .orElse(null);
        return (profile != null) ? mapperDto(profile) : null;
    }

    @Override
    public List<ModuleResponse> getModulesByProfile(String profileId) {
        UUID id = UUID.fromString(profileId);
        ProfileEntity profile = profileRepository.findByIdWithModules(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, profileId)));

        Set<ModuleEntity> allowedModules = profile.getModules();

        return profile.getModules().stream()
                .filter(module -> module.getParent() == null) // Inicia desde los módulos raíz
                .map(module -> convertToDto(module, allowedModules))
                .toList();
    }

    private ModuleResponse convertToDto(ModuleEntity module, Set<ModuleEntity> allowedModules) {
        return ModuleResponse.builder()
                .id(module.getId())
                .name(module.getName())
                .route(module.getRoute())
                .iconName(module.getIconName())
                .isParent(module.getIsParent())
                .erased(module.getErased())
                .children(module.getChildren().stream()
                        .filter(allowedModules::contains)
                        .map(child -> convertToDto(child, allowedModules)).toList())
                .build();
    }

}
