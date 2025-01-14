package com.alness.lifemaster.profiles.service.impl;

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
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.profiles.dto.request.ProfileRequest;
import com.alness.lifemaster.profiles.dto.response.ProfileResponse;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.profiles.service.ProfileService;
import com.alness.lifemaster.profiles.specification.ProfileSpecification;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    @Autowired
    private ProfileRepository profileRepository;

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public ProfileResponse save(ProfileRequest request) {
        ProfileEntity newProfile = modelMapper.map(request, ProfileEntity.class);
        try {
            newProfile = profileRepository.save(newProfile);
            return mapperDto(newProfile);
        } catch (Exception e) {
            log.error("Error to save profile ", e);
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
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
                .stream().map(this::mapperDto).toList();
    }

    @Override
    public ProfileResponse update(String id, ProfileRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private ProfileResponse mapperDto(ProfileEntity source) {
        return modelMapper.map(source, ProfileResponse.class);
    }

    public Specification<ProfileEntity> filterWithParameters(Map<String, String> parameters) {
        return new ProfileSpecification().getSpecificationByFilters(parameters);
    }

    @Override
    public ProfileResponse findByName(String name) {
        ProfileEntity profile = profileRepository.findOne(filterWithParameters(Map.of(Filters.KEY_NAME, name)))
                .orElse(null);
        if (profile == null) {
            return null;
        }
        return mapperDto(profile);
    }

    @Override
    public List<ModuleResponse> getModulesByProfile(String profileId) {
        UUID id = UUID.fromString(profileId);
        log.info("profile id: {}", id);
        ProfileEntity profile = profileRepository.findByIdWithModules(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return profile.getModules().stream()
                .filter(module -> module.getParent() == null) // Inicia desde los módulos raíz
                .map(this::convertToDto)
                .toList();
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

}
