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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.files.FilePurpose;
import com.alness.lifemaster.files.StoredFileResponse;
import com.alness.lifemaster.files.StoredFileService;
import com.alness.lifemaster.nutrition.dto.request.FoodRequest;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionPhotoContent;
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
@Transactional
public class NutritionServiceImpl implements NutritionService {
    private final NutritionRepository nutritionRepository;
    private final UserRepository userRepository;
    private final StoredFileService storedFileService;

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
        return save(userId, request, null);
    }

    @Override
    public NutritionResponse save(String userId, NutritionRequest request, MultipartFile photo) {
        UUID uuid = UUID.fromString(userId);
        UserEntity user = userRepository.findById(uuid)
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));

        try {
            NutritionEntity nutrition = modelMapper.map(request, NutritionEntity.class);
            nutrition.setUser(user);

            nutrition.setFood(mapFood(request.getFood(), nutrition));
            applyPhoto(uuid, nutrition, photo, false);
            return mapperDto(nutritionRepository.save(nutrition));

        } catch (RestExceptionHandler ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);
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
        NutritionEntity nutrition = findActiveOwned(userId, id);
        return mapperDto(nutrition);
    }

    @Override
    public NutritionResponse update(String userId, String id, NutritionRequest request) {
        return update(userId, id, request, null);
    }

    @Override
    public NutritionResponse update(String userId, String id, NutritionRequest request, MultipartFile photo) {
        NutritionEntity existing = findActiveOwned(userId, id);
        try {
            existing.setName(request.getName());
            existing.setMealType(request.getMealType());
            existing.setNotes(request.getNotes());
            existing.setDateTimeConsumption(DateTimeUtils.parseToLocalDateTime(request.getDateTimeConsumption()));

            if (existing.getFood() == null) {
                existing.setFood(new ArrayList<>());
            } else {
                existing.getFood().clear();
            }
            existing.getFood().addAll(mapFood(request.getFood(), existing));
            applyPhoto(UUID.fromString(userId), existing, photo, Boolean.TRUE.equals(request.getRemovePhoto()));
            return mapperDto(nutritionRepository.save(existing));

        } catch (RestExceptionHandler ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        NutritionEntity nutrition = findActiveOwned(userId, id);
        if (nutrition.getPhoto() != null) {
            UUID photoId = nutrition.getPhoto().getId();
            nutrition.setPhoto(null);
            storedFileService.delete(UUID.fromString(userId), photoId);
        }
        nutrition.setErased(true);
        nutritionRepository.save(nutrition);
        return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
    }

    private NutritionEntity findActiveOwned(String userId, String id) {
        return nutritionRepository
                .findByIdAndUserIdAndErasedFalse(UUID.fromString(id), UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
    }

    private NutritionResponse mapperDto(NutritionEntity source) {
        NutritionResponse response = modelMapper.map(source, NutritionResponse.class);
        response.setPhotoId(source.getPhoto() == null ? null : source.getPhoto().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionPhotoContent photo(String userId, String id) {
        NutritionEntity nutrition = findActiveOwned(userId, id);
        if (nutrition.getPhoto() == null) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                    "El registro no tiene una fotografia.");
        }
        UUID userUuid = UUID.fromString(userId);
        var metadata = storedFileService.findOwned(userUuid, nutrition.getPhoto().getId());
        return new NutritionPhotoContent(storedFileService.content(userUuid, metadata.getId()),
                metadata.getContentType(), metadata.getOriginalName());
    }

    @Override
    public ResponseServerDto deletePhoto(String userId, String id) {
        NutritionEntity nutrition = findActiveOwned(userId, id);
        if (nutrition.getPhoto() == null) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                    "El registro no tiene una fotografia.");
        }
        UUID photoId = nutrition.getPhoto().getId();
        nutrition.setPhoto(null);
        nutritionRepository.save(nutrition);
        storedFileService.delete(UUID.fromString(userId), photoId);
        return new ResponseServerDto("Fotografia eliminada correctamente.", HttpStatus.ACCEPTED, true);
    }

    private List<FoodEntity> mapFood(List<FoodRequest> food, NutritionEntity nutrition) {
        if (food == null || food.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(food.stream().map(item -> {
            FoodEntity entity = modelMapper.map(item, FoodEntity.class);
            entity.setNutrition(nutrition);
            return entity;
        }).toList());
    }

    private void applyPhoto(UUID userId, NutritionEntity nutrition, MultipartFile photo, boolean removePhoto) {
        if (photo != null && !photo.isEmpty()) {
            if (nutrition.getPhoto() == null) {
                StoredFileResponse saved = storedFileService.save(userId, FilePurpose.NUTRITION_IMAGE, photo);
                nutrition.setPhoto(storedFileService.findOwned(userId, saved.id()));
            } else {
                storedFileService.replace(userId, nutrition.getPhoto().getId(), FilePurpose.NUTRITION_IMAGE, photo);
            }
            return;
        }
        if (removePhoto && nutrition.getPhoto() != null) {
            UUID photoId = nutrition.getPhoto().getId();
            nutrition.setPhoto(null);
            storedFileService.delete(userId, photoId);
        }
    }

    public Specification<NutritionEntity> filterWithParameters(Map<String, String> params) {
        return new NutritionSpec().getSpecificationByFilters(params);
    }

}
