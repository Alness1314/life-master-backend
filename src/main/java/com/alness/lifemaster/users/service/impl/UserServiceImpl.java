package com.alness.lifemaster.users.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.dto.ValueExistenceResponse;
import com.alness.lifemaster.common.validation.GenericExistenceValidator;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.common.enums.AllowedProfiles;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.files.StoredFileRepository;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.modules.entity.ModuleEntity;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.permissions.service.EffectivePermission;
import com.alness.lifemaster.permissions.service.EffectivePermissionService;
import com.alness.lifemaster.users.dto.CustomUser;
import com.alness.lifemaster.users.dto.request.UserRequest;
import com.alness.lifemaster.users.dto.request.UserSelfUpdateRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.users.service.UserService;
import com.alness.lifemaster.users.specification.UserSpecification;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.LoggerUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
   
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenericMapper mapper;
    private final GenericExistenceValidator existenceValidator;
    private final StoredFileRepository storedFileRepository;
    private final EffectivePermissionService effectivePermissionService;

    @Override
    public UserResponse save(UserRequest request) {
        if (userRepository.existsByUsernameAndErasedFalse(request.getUsername().trim())) {
            throw new RestExceptionHandler(
                    ApiCodes.API_CODE_409,
                    HttpStatus.CONFLICT,
                    Messages.USER_ALREADY_REGISTERED);
        }
        UserEntity newUser = mapper.map(request, UserEntity.class);
        try {
            if (request.getProfiles() == null) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, Messages.NOT_FOUND_BASIC);
            }

            List<ProfileEntity> profiles = new ArrayList<>();
            for (String profileName : request.getProfiles()) {
                ProfileEntity profile = profileRepository.findById(UUID.fromString(profileName)).orElse(null);
                profiles.add(profile);
            }
            newUser.setProfiles(profiles);
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            if (request.getImageId() != null && !request.getImageId().isEmpty()) {
                newUser.setImageId(UUID.fromString(request.getImageId()));
            } else {
                newUser.setImageId(null);
            }
            newUser = userRepository.saveAndFlush(newUser);
            return mapperDto(newUser);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_409,
                        HttpStatus.CONFLICT, Messages.USER_ALREADY_REGISTERED);
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, Messages.DATA_INTEGRITY, ex);
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
    public UserResponse findOne(String id) {
        UserEntity findUser = userRepository.findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Messages.NOT_FOUND, id)));
        return mapperDto(findUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findCurrentUser(UUID userId) {
        return mapperDto(findActiveUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> findCurrentUserModules(UUID userId, String level) {
        UserEntity user = findActiveUser(userId);
        Map<UUID, ModuleEntity> modules = new LinkedHashMap<>();
        boolean administrator = user.getProfiles().stream()
                .anyMatch(profile -> AllowedProfiles.ADMIN.getName().equals(profile.getName()));
        Map<UUID, EffectivePermission> permissions = administrator
                ? Map.of()
                : effectivePermissionService.findEffectivePermissions(user);

        user.getProfiles().stream()
                .filter(profile -> !Boolean.TRUE.equals(profile.getErased()))
                .flatMap(profile -> profile.getModules().stream())
                .filter(module -> !Boolean.TRUE.equals(module.getErased()))
                .filter(module -> level == null || level.isBlank()
                        || level.equalsIgnoreCase(module.getLevel()))
                .filter(module -> administrator
                        || permissions.getOrDefault(module.getId(), denied(module.getId())).canRead())
                .forEach(module -> modules.putIfAbsent(module.getId(), module));

        return modules.values().stream()
                .map(module -> mapModuleDto(module, administrator
                        ? allowed(module.getId())
                        : permissions.get(module.getId())))
                .toList();
    }

    @Override
    public List<UserResponse> find(Map<String, String> params) {
        return userRepository.findAll(filterWithParameters(params))
                .stream().map(this::mapperDto).toList();
    }

    @Override
    public UserResponse update(String id, UserRequest request) {
        try {
            UUID userId = UUID.fromString(id);
            if (userRepository.existsByUsernameAndErasedFalseAndIdNot(request.getUsername().trim(), userId)) {
                throw new RestExceptionHandler(
                        ApiCodes.API_CODE_409,
                        HttpStatus.CONFLICT,
                        Messages.USER_ALREADY_REGISTERED);
            }
            // Buscar el usuario existente por su ID
            UserEntity existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, String.format(Messages.NOT_FOUND, id)));

            // Actualizar los campos del usuario existente con los valores de la solicitud
            mapper.map(request, existingUser);

            // Actualizar los perfiles del usuario
            List<ProfileEntity> profiles = new ArrayList<>();
            for (String profileName : request.getProfiles()) {
                ProfileEntity profile = profileRepository.findById(UUID.fromString(profileName))
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, String.format(Messages.NOT_FOUND, profileName)));
                profiles.add(profile);
            }
            existingUser.setProfiles(profiles);

            // Si se proporciona una nueva contraseña, codificarla y actualizarla
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            // Lógica para manejar la imagen (si es necesario)
            if (request.getImageId() != null && !request.getImageId().isEmpty()) {
                UUID imageId = UUID.fromString(request.getImageId());
                if (!storedFileRepository.existsByIdAndUserIdAndErasedFalse(imageId, userId)) {
                    throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                            "La imagen indicada no pertenece al usuario.");
                }
                existingUser.setImageId(imageId);
            }

            // Guardar los cambios en la base de datos
            existingUser = userRepository.saveAndFlush(existingUser);

            // Mapear y devolver la respuesta
            return mapperDto(existingUser);

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_409,
                        HttpStatus.CONFLICT, Messages.USER_ALREADY_REGISTERED);
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, Messages.DATA_INTEGRITY, ex);
        } catch (RestExceptionHandler ex) {
            LoggerUtil.logError(ex);
            throw ex; // Re-lanzar excepciones ya gestionadas
        } catch (Exception ex) {
            LoggerUtil.logError(ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, Messages.ERROR_ENTITY_UPDATE, ex);
        }
    }

    @Override
    public UserResponse updateSelf(UUID userId, UserSelfUpdateRequest request) {
        UserEntity existingUser = userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getErased()))
                .orElseThrow(() -> new RestExceptionHandler(
                        ApiCodes.API_CODE_404,
                        HttpStatus.NOT_FOUND,
                        Messages.NOT_FOUND_BASIC));

        boolean hasChanges = false;
        if (request.getFullName() != null) {
            existingUser.setFullName(request.getFullName().trim());
            hasChanges = true;
        }
        if (request.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            hasChanges = true;
        }
        if (request.getImageId() != null) {
            UUID imageId = UUID.fromString(request.getImageId());
            if (!storedFileRepository.existsByIdAndUserIdAndErasedFalse(imageId, userId)) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                        "La imagen indicada no pertenece al usuario.");
            }
            existingUser.setImageId(imageId);
            hasChanges = true;
        }
        if (!hasChanges) {
            throw new RestExceptionHandler(
                    ApiCodes.API_CODE_400,
                    HttpStatus.BAD_REQUEST,
                    "Debes enviar al menos un campo permitido para actualizar.");
        }

        try {
            return mapperDto(userRepository.saveAndFlush(existingUser));
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(
                    ApiCodes.API_CODE_400,
                    HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (RestExceptionHandler ex) {
            throw ex;
        } catch (Exception ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(
                    ApiCodes.API_CODE_500,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ValueExistenceResponse validateExistingValues(Map<String, String> values, UUID excludeId) {
        return existenceValidator.validate(
                values,
                Map.of("username", username -> excludeId == null
                        ? userRepository.existsByUsernameAndErasedFalse(username)
                        : userRepository.existsByUsernameAndErasedFalseAndIdNot(username, excludeId)));
    }

    @Override
    public ResponseServerDto delete(String id) {
        UserEntity findUser = userRepository.findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Messages.NOT_FOUND, id)));
        try {
            findUser.setErased(true);
            userRepository.save(findUser);
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

    private UserResponse mapperDto(UserEntity source) {
        return mapper.map(source, UserResponse.class);
    }

    private UserEntity findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getErased()))
                .orElseThrow(() -> new RestExceptionHandler(
                        ApiCodes.API_CODE_404,
                        HttpStatus.NOT_FOUND,
                        Messages.NOT_FOUND_BASIC));
    }

    private ModuleDto mapModuleDto(ModuleEntity module, EffectivePermission permission) {
        return ModuleDto.builder()
                .id(module.getId().toString())
                .name(module.getName())
                .route(module.getRoute())
                .permissionKey(module.getPermissionKey())
                .iconName(module.getIconName())
                .level(module.getLevel())
                .description(module.getDescription())
                .erased(module.getErased())
                .isParent(module.getIsParent())
                .canCreate(permission.canCreate())
                .canRead(permission.canRead())
                .canUpdate(permission.canUpdate())
                .canDelete(permission.canDelete())
                .build();
    }

    private EffectivePermission allowed(UUID moduleId) {
        return new EffectivePermission(moduleId, true, true, true, true);
    }

    private EffectivePermission denied(UUID moduleId) {
        return new EffectivePermission(moduleId, false, false, false, false);
    }

    public Specification<UserEntity> filterWithParameters(Map<String, String> parameters) {
        return new UserSpecification().getSpecificationByFilters(parameters);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Specification<UserEntity> specification = filterWithParameters(
                Map.of(Filters.KEY_USERNAME, username, Filters.KEY_ERASED, "false"));
        UserEntity user = userRepository.findOne(specification).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, username)));
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getProfiles().forEach(profile -> authorities.add(new SimpleGrantedAuthority(profile.getName())));
        return new CustomUser(user.getUsername(), user.getPassword(), authorities, user.getId());
    }

    @Override
    public UserResponse findByUsername(String username) {
        UserEntity findUser = userRepository.findOne(filterWithParameters(Map.of(Filters.KEY_USERNAME, username)))
                .orElse(null);
        if (findUser == null) {
            return null;
        }
        return mapperDto(findUser);
    }
}
