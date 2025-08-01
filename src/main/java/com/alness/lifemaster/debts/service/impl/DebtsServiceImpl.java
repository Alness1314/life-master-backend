package com.alness.lifemaster.debts.service.impl;

import java.time.LocalDate;
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
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.debts.dto.request.DebtsRequest;
import com.alness.lifemaster.debts.dto.request.PaymentRequest;
import com.alness.lifemaster.debts.dto.response.DebtsResponse;
import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.DebtsService;
import com.alness.lifemaster.debts.spec.DebtsSpec;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
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
public class DebtsServiceImpl implements DebtsService {

    private final DebtsRespository debtsRespository;
    private final UserRepository userRepository;

    private ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    private void init() {
        configureModelMapper();

        Converter<String, LocalDate> localDateConverter = createConverter(DateTimeUtils::parseToLocalDate);

        modelMapper.createTypeMap(DebtsRequest.class, DebtsEntity.class)
                .addMappings(mpi -> mpi.using(localDateConverter).map(DebtsRequest::getDueDate,
                        DebtsEntity::setDueDate));
        modelMapper.createTypeMap(PaymentRequest.class, PaymentsEntity.class)
                .addMappings(amp -> amp.using(localDateConverter).map(PaymentRequest::getPaymentDate,
                        PaymentsEntity::setPaymentDate));
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
    public DebtsResponse save(String userId, DebtsRequest request) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));

        try {
            DebtsEntity debts = modelMapper.map(request, DebtsEntity.class);

            List<PaymentsEntity> paymentsList = new ArrayList<>();
            for (PaymentRequest payment : request.getPayments()) {
                PaymentsEntity paymentEntity = modelMapper.map(payment,
                        PaymentsEntity.class);
                paymentEntity.setDebts(debts);
                paymentsList.add(paymentEntity);
            }

            debts.setPayments(paymentsList);
            debts.setUser(user);
            debts = debtsRespository.save(debts);
            return mapperDto(debts);
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
    public List<DebtsResponse> find(String userId, Map<String, String> params) {
        return debtsRespository.findAll(filterWithParameters(FuncUtils.integrateUser(userId, params)))
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public DebtsResponse findOne(String userId, String id) {
        DebtsEntity debts = debtsRespository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(debts);
    }

    @Override
    public DebtsResponse update(String userId, String id, DebtsRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private DebtsResponse mapperDto(DebtsEntity source) {
        return modelMapper.map(source, DebtsResponse.class);
    }

    public Specification<DebtsEntity> filterWithParameters(Map<String, String> params) {
        return new DebtsSpec().getSpecificationByFilters(params);
    }

}
