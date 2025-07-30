package com.alness.lifemaster.nutrition.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.nutrition.dto.request.FoodRequest;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionResponse;
import com.alness.lifemaster.nutrition.entity.FoodEntity;
import com.alness.lifemaster.nutrition.entity.NutritionEntity;
import com.alness.lifemaster.nutrition.repository.NutritionRepository;
import com.alness.lifemaster.nutrition.service.NutritionService;
import com.alness.lifemaster.nutrition.spec.NutritionSpec;
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
public class NutritionServiceImpl implements NutritionService {
    private final NutritionRepository nutritionRepository;
    private final UserRepository userRepository;

    ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {

        configureModelMapper();

        Converter<String, LocalDateTime> localDateTimeConverter = createConverter(DateTimeUtils::parseToLocalDateTime);

        modelMapper.createTypeMap(NutritionRequest.class, NutritionEntity.class)
                .addMappings(mpi -> mpi.using(localDateTimeConverter).map(NutritionRequest::getDateTimeConsumption,
                        NutritionEntity::setDateTimeConsumption));
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
    public NutritionResponse save(String userId, NutritionRequest request) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));
        try {
            NutritionEntity newNutri = modelMapper.map(request, NutritionEntity.class);

            List<FoodEntity> foodList = new ArrayList<>();
            for (FoodRequest food : request.getFood()) {
                FoodEntity foodEntity = modelMapper.map(food,
                        FoodEntity.class);
                foodEntity.setNutrition(newNutri);
                foodList.add(foodEntity);
            }

            newNutri.setFood(foodList);
            newNutri.setUser(user);
            newNutri = nutritionRepository.save(newNutri);
            return mapperDto(newNutri);
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
    public List<NutritionResponse> find(String userId, Map<String, String> params) {
        return nutritionRepository.findAll(filterWithParameters(FuncUtils.integrateUser(userId, params)))
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public NutritionResponse findOne(String userId, String id) {
        NutritionEntity nutrition = nutritionRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(nutrition);
    }

    @Override
    public NutritionResponse update(String userId, String id, NutritionRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private NutritionResponse mapperDto(NutritionEntity source) {
        return modelMapper.map(source, NutritionResponse.class);
    }

    public Specification<NutritionEntity> filterWithParameters(Map<String, String> params) {
        return new NutritionSpec().getSpecificationByFilters(params);
    }

}
