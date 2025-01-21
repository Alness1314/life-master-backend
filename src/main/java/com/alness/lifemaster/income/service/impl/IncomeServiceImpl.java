package com.alness.lifemaster.income.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.dto.response.IncomeResponse;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.income.service.IncomeService;
import com.alness.lifemaster.income.specification.IncomeSpecification;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.DateTimeUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IncomeServiceImpl implements IncomeService {
    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    private ModelMapper mapper = new ModelMapper();

    @PostConstruct
    public void init() {
        configureModelMapper();
        Converter<String, LocalDate> localDateConverter = createConverter(DateTimeUtils::parseToLocalDate);

        mapper.createTypeMap(IncomeRequest.class, IncomeEntity.class)
                .addMappings(
                        mpa -> mpa.using(localDateConverter).map(IncomeRequest::getPaymentDate,
                                IncomeEntity::setPaymentDate));
    }

    private void configureModelMapper() {
        mapper.getConfiguration()
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
    public List<IncomeResponse> find(String userId, Map<String, String> params) {
        params.put("user", userId);
        Specification<IncomeEntity> specification = filterWithParameters(params);
        return incomeRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .collect(Collectors.toList());
    }

    @Override
    public IncomeResponse findOne(String userId, String id) {
        IncomeEntity income = incomeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Income not found."));
        return mapperDto(income);
    }

    @Override
    public IncomeResponse save(String userId, IncomeRequest request) {
        IncomeEntity income = mapper.map(request, IncomeEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(
                        () -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "User not found."));
        income.setUser(user);
        try {
            income = incomeRepository.save(income);
        } catch (Exception e) {
            log.error("Error to save income {}", e.getMessage());
            e.printStackTrace();
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to save invome.");
        }
        return mapperDto(income);
    }

    @Override
    public IncomeResponse update(String userId, String id, IncomeRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private IncomeResponse mapperDto(IncomeEntity source) {
        return mapper.map(source, IncomeResponse.class);
    }

    public Specification<IncomeEntity> filterWithParameters(Map<String, String> parameters) {
        return new IncomeSpecification().getSpecificationByFilters(parameters);
    }
}
