package com.alness.lifemaster.assistance.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.assistance.repository.AssistanceRepository;
import com.alness.lifemaster.assistance.service.AssistanceService;
import com.alness.lifemaster.assistance.specification.AssistanceSpecification;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.DateTimeUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssistanceServiceImpl implements AssistanceService {
    @Autowired
    private AssistanceRepository assistanceRepository;

    @Autowired
    private UserRepository userRepository;

    ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {

        configureModelMapper();

        Converter<String, LocalDate> localDateConverter = createConverter(DateTimeUtils::parseToLocalDate);
        Converter<String, LocalTime> localTimeConverter = createConverter(DateTimeUtils::parseToLocalTime);
        Converter<LocalTime, String> localTimeToStringConverter = createConverter(
                DateTimeUtils::parseLocalTimeToString);

        modelMapper.createTypeMap(AssistanceRequest.class, AssistanceEntity.class)
                .addMappings(mpi -> {
                    mpi.using(localTimeConverter).map(AssistanceRequest::getDepartureTime,
                            AssistanceEntity::setDepartureTime);
                    mpi.using(localTimeConverter).map(AssistanceRequest::getTimeEntry, AssistanceEntity::setTimeEntry);
                    mpi.using(localDateConverter).map(AssistanceRequest::getWorkDate, AssistanceEntity::setWorkDate);
                });
        modelMapper.createTypeMap(AssistanceEntity.class, AssistanceResponse.class)
                .addMappings(amp -> {
                    amp.using(localTimeToStringConverter).map(AssistanceEntity::getDepartureTime,
                            AssistanceResponse::setDepartureTime);
                    amp.using(localTimeToStringConverter).map(AssistanceEntity::getTimeEntry,
                            AssistanceResponse::setTimeEntry);
                });
    }

    private void configureModelMapper() {
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private <S, D> Converter<S, D> createConverter(Function<S, D> converterFunction) {
        return new AbstractConverter<>() {
            @Override
            protected D convert(S source) {
                return source == null ? null : converterFunction.apply(source);
            }
        };
    }

    @Override
    public AssistanceResponse findOne(String userId, String id) {
        AssistanceEntity assistance = assistanceRepository
                .findOne(filterWithParameters(Map.of("user", userId, "id", id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Entity not found"));
        return mapperDto(assistance);

    }

    @Override
    public List<AssistanceResponse> find(String userId, Map<String, String> params) {
        return assistanceRepository.findAll(filterWithParameters(addUserIdToParams(userId, params)))
                .stream().map(this::mapperDto)
                .toList();
    }

    /**
     * 
     * @param userId userId El ID del usuario.
     * @param params Los parámetros originales.
     * @return Un nuevo mapa con el ID de usuario agregado
     */
    private Map<String, String> addUserIdToParams(String userId, Map<String, String> params) {
        Map<String, String> updatedParams = new HashMap<>(params);
        updatedParams.put("user", userId);
        return updatedParams;
    }

    @Override
    public AssistanceResponse save(String userId, AssistanceRequest request) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format("user with id %s not found.", userId)));
        try {
            AssistanceEntity newAssistance = modelMapper.map(request, AssistanceEntity.class);
            newAssistance.setUser(user);
            newAssistance = assistanceRepository.save(newAssistance);
            return mapperDto(newAssistance);
        } catch (Exception e) {
            log.error("Error {}", e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to save assistance");
        }
    }

    @Override
    public AssistanceResponse update(String userId, String id, AssistanceRequest request) {
        // Buscar al usuario por su ID
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format("user with id %s not found.", userId)));

        try {
            // Buscar la asistencia existente por su ID
            AssistanceEntity existingAssistance = assistanceRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                            String.format("assistance with id %s not found.", id)));

            // Verificar que la asistencia pertenece al usuario
            if (!existingAssistance.getUser().getId().equals(user.getId())) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_403, HttpStatus.FORBIDDEN,
                        String.format("User with id %s is not allowed to update this assistance.", userId));
            }

            // Mapear los datos de la solicitud a la entidad de asistencia
            modelMapper.map(request, existingAssistance); // Esto actualizará los valores del objeto existingAssistance

            // Establecer el usuario en la entidad (por si hubo cambios en el request)
            existingAssistance.setUser(user);

            // Guardar la asistencia actualizada en la base de datos
            existingAssistance = assistanceRepository.save(existingAssistance);

            // Retornar la respuesta de la asistencia actualizada
            return mapperDto(existingAssistance);
        } catch (Exception e) {
            log.error("Error {}", e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to update assistance");
        }
    }

    @Override
    public ResponseDto delete(String userId, String id) {
        AssistanceEntity assistance = assistanceRepository
                .findOne(filterWithParameters(Map.of("user", userId, "id", id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Entity not found"));

        try {
            assistanceRepository.delete(assistance);
            return new ResponseDto("Registro de asistencia eliminado", HttpStatus.OK, true);
        } catch (Exception e) {
            log.error("error al borrar registro asistencia", e);
            return new ResponseDto("No se pudo eliminar el registro de asistencia", HttpStatus.METHOD_NOT_ALLOWED,
                    false);
        }
    }

    private AssistanceResponse mapperDto(AssistanceEntity source) {
        return modelMapper.map(source, AssistanceResponse.class);
    }

    public Specification<AssistanceEntity> filterWithParameters(Map<String, String> params) {
        return new AssistanceSpecification().getSpecificationByFilters(params);
    }

    @Override
    public AssistanceResponse assignOutput(String userId, String id, String departureTime) {
        AssistanceEntity assistance = assistanceRepository
                .findOne(filterWithParameters(Map.of("user", userId, "id", id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Entity not found"));
        try {
            assistance.setDepartureTime(DateTimeUtils.parseToLocalTime(departureTime));
            assistance = assistanceRepository.save(assistance);
            return mapperDto(assistance);
        } catch (Exception e) {
            log.error("Error", e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to update assistance");
        }
    }
}
