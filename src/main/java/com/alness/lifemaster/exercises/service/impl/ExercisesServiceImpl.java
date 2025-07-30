package com.alness.lifemaster.exercises.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.exercises.dto.ExercisesRequest;
import com.alness.lifemaster.exercises.dto.ExercisesResponse;
import com.alness.lifemaster.exercises.entity.ExercisesEntity;
import com.alness.lifemaster.exercises.repository.ExercisesRepository;
import com.alness.lifemaster.exercises.service.ExercisesService;
import com.alness.lifemaster.exercises.spec.ExercisesSpec;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.DateTimeUtils;
import com.alness.lifemaster.utils.FuncUtils;
import com.alness.lifemaster.utils.LoggerUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExercisesServiceImpl implements ExercisesService {
    private final ExercisesRepository exercisesRepository;
    private final UserRepository userRepository;

    ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {

        configureModelMapper();

        Converter<String, LocalDate> localDateConverter = createConverter(DateTimeUtils::parseToLocalDate);
        Converter<String, LocalTime> localTimeConverter = createConverter(DateTimeUtils::parseToLocalTime);
        Converter<LocalTime, String> localTimeToStringConverter = createConverter(
                DateTimeUtils::parseLocalTimeToString);

        modelMapper.createTypeMap(ExercisesRequest.class, ExercisesEntity.class)
                .addMappings(mpi -> {
                    mpi.using(localTimeConverter).map(ExercisesRequest::getStartTime,
                            ExercisesEntity::setStartTime);
                    mpi.using(localTimeConverter).map(ExercisesRequest::getEndTime, ExercisesEntity::setEndTime);
                    mpi.using(localDateConverter).map(ExercisesRequest::getTrainingDate,
                            ExercisesEntity::setTrainingDate);
                });
        modelMapper.createTypeMap(ExercisesEntity.class, ExercisesResponse.class)
                .addMappings(amp -> {
                    amp.using(localTimeToStringConverter).map(ExercisesEntity::getStartTime,
                            ExercisesResponse::setStartTime);
                    amp.using(localTimeToStringConverter).map(ExercisesEntity::getEndTime,
                            ExercisesResponse::setEndTime);
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
    public ExercisesResponse save(String userId, ExercisesRequest request) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));
        try {
            ExercisesEntity newExercises = modelMapper.map(request, ExercisesEntity.class);
            newExercises.setUser(user);
            newExercises = exercisesRepository.save(newExercises);
            return mapperDto(newExercises);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (InvalidDataAccessResourceUsageException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error en la sintaxis de acceso a datos: " + ex.getMessage());
        } catch (Exception e) {
            LoggerUtil.logError(e);
            e.printStackTrace();
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_SAVE);
        }
    }

    @Override
    public List<ExercisesResponse> find(String userId, Map<String, String> params) {
        return exercisesRepository.findAll(filterWithParameters(FuncUtils.integrateUser(userId, params)))
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public ExercisesResponse findOne(String userId, String id) {
        ExercisesEntity exercises = exercisesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(exercises);
    }

    @Override
    public ExercisesResponse update(String userId, String id, ExercisesRequest request) {
        ExercisesEntity existing = exercisesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        try {
            // Actualizamos campos del request, pero sin sobreescribir user, id ni createAt
            modelMapper.map(request, existing); // cuidado: asegúrate que el mapper no sobreescriba campos no deseados

            ExercisesEntity updated = exercisesRepository.save(existing);
            return mapperDto(updated);
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
    public ResponseServerDto delete(String userId, String id) {
        ExercisesEntity exercises = exercisesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        try {
            exercisesRepository.delete(exercises);
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

    private ExercisesResponse mapperDto(ExercisesEntity source) {
        return modelMapper.map(source, ExercisesResponse.class);
    }

    public Specification<ExercisesEntity> filterWithParameters(Map<String, String> params) {
        return new ExercisesSpec().getSpecificationByFilters(params);
    }

}
