package com.alness.lifemaster.users.service.impl;

import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.web.server.ResponseStatusException;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.users.dto.CustomUser;
import com.alness.lifemaster.users.dto.request.UserRequest;
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
public class UserServiceImpl implements UserService, UserDetailsService {
   
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenericMapper mapper;

    @Override
    public UserResponse save(UserRequest request) {
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
                // logica para buscar la imagen
            } else {
                newUser.setId(null);
            }
            newUser = userRepository.save(newUser);
            return mapperDto(newUser);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_412,
                        HttpStatus.PRECONDITION_FAILED, Messages.USER_ALREADY_REGISTERED);
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
    public List<UserResponse> find(Map<String, String> params) {
        return userRepository.findAll(filterWithParameters(params))
                .stream().map(this::mapperDto).toList();
    }

    @Override
    public UserResponse update(String id, UserRequest request) {
        try {
            // Buscar el usuario existente por su ID
            UserEntity existingUser = userRepository.findById(UUID.fromString(id))
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
                // Lógica para buscar y actualizar la imagen
            }

            // Guardar los cambios en la base de datos
            existingUser = userRepository.save(existingUser);

            // Mapear y devolver la respuesta
            return mapperDto(existingUser);

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_412,
                        HttpStatus.PRECONDITION_FAILED, Messages.USER_ALREADY_REGISTERED);
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
