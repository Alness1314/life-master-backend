package com.alness.lifemaster.income.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.dto.response.IncomeResponse;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.income.service.IncomeService;
import com.alness.lifemaster.income.specification.IncomeSpecification;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.FuncUtils;
import com.alness.lifemaster.utils.LoggerUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final GenericMapper mapper;

    @Override
    public List<IncomeResponse> find(String userId, Map<String, String> params) {
        Specification<IncomeEntity> specification = filterWithParameters(FuncUtils.integrateUser(userId, params));
        return incomeRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public IncomeResponse findOne(String userId, String id) {
        IncomeEntity income = incomeRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(income);
    }

    @Override
    public IncomeResponse save(String userId, IncomeRequest request) {
        IncomeEntity income = mapper.map(request, IncomeEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(
                        () -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                                String.format(Messages.NOT_FOUND, userId)));
        income.setUser(user);
        try {
            income = incomeRepository.save(income);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
        return mapperDto(income);
    }

    @Override
    public IncomeResponse update(String userId, String id, IncomeRequest request) {
        // Buscar ingreso existente
        IncomeEntity existingIncome = incomeRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        // Verificar que el ingreso pertenece al usuario
        if (!existingIncome.getUser().getId().toString().equals(userId)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_403, HttpStatus.FORBIDDEN,
                    Messages.FORBIDEN_UPDATE_DATA);
        }

        // Mapear nuevos datos al ingreso existente
        mapper.map(request, existingIncome);

        try {
            existingIncome = incomeRepository.save(existingIncome);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }

        return mapperDto(existingIncome);
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        IncomeEntity income = incomeRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        try {
            incomeRepository.delete(income);
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

    private IncomeResponse mapperDto(IncomeEntity source) {
        return mapper.map(source, IncomeResponse.class);
    }

    public Specification<IncomeEntity> filterWithParameters(Map<String, String> parameters) {
        return new IncomeSpecification().getSpecificationByFilters(parameters);
    }
}
