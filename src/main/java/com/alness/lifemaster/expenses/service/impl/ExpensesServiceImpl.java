package com.alness.lifemaster.expenses.service.impl;

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

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.dto.response.ExpensesResponse;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.expenses.service.ExpensesService;
import com.alness.lifemaster.expenses.specification.ExpensesSpecification;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.DateTimeUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExpensesServiceImpl implements ExpensesService {
    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ModelMapper mapper = new ModelMapper();

    @PostConstruct
    public void init() {
        configureModelMapper();
        Converter<String, LocalDate> localDateConverter = createConverter(DateTimeUtils::parseToLocalDate);

        mapper.createTypeMap(ExpensesRequest.class, ExpensesEntity.class)
                .addMappings(
                        mpa -> mpa.using(localDateConverter).map(ExpensesRequest::getPaymentDate,
                                ExpensesEntity::setPaymentDate));
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
    public List<ExpensesResponse> find(String userId, Map<String, String> params) {
        params.put("user", userId);
        Specification<ExpensesEntity> specification = filterWithParameters(params);
        return expensesRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExpensesResponse findOne(String userId, String id) {
        ExpensesEntity expenses = expensesRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Expenses not found."));
        return mapperDto(expenses);
    }

    @Override
    public ExpensesResponse save(String userId, ExpensesRequest request) {
        ExpensesEntity expenses = mapper.map(request, ExpensesEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "User not found."));
        expenses.setUser(user);
        CategoryEntity category = categoryRepository.findById(UUID.fromString(request.getCategory()))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "category not found."));
        expenses.setCategory(category);
        try {
            expenses = expensesRepository.save(expenses);
        } catch (Exception e) {
            log.error("Error to save expenses {}", e.getMessage());
            e.printStackTrace();
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR, "Error to save expenses.");
        }
        return mapperDto(expenses);
    }

    @Override
    public ExpensesResponse update(String userId, String id, ExpensesRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private ExpensesResponse mapperDto(ExpensesEntity expenses) {
        return mapper.map(expenses, ExpensesResponse.class);
    }

    public Specification<ExpensesEntity> filterWithParameters(Map<String, String> parameters) {
        return new ExpensesSpecification().getSpecificationByFilters(parameters);
    }

}
