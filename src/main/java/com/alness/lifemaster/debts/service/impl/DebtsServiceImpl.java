package com.alness.lifemaster.debts.service.impl;

import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;

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
import com.alness.lifemaster.debts.service.DebtCalculator;
import com.alness.lifemaster.debts.spec.DebtsSpec;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.DateTimeUtils;
import com.alness.lifemaster.utils.FuncUtils;
import com.alness.lifemaster.utils.LoggerUtil;
import com.alness.lifemaster.finance.paymentmethod.PaymentMethodService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DebtsServiceImpl implements DebtsService {

    private final DebtsRespository debtsRespository;
    private final UserRepository userRepository;
    private final PaymentMethodService paymentMethodService;

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
        UUID uuid = UUID.fromString(userId);
        UserEntity user = userRepository.findById(uuid)
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));

        try {
            DebtsEntity debts = modelMapper.map(request, DebtsEntity.class);
            debts.setUser(user);

            List<PaymentsEntity> paymentsList = request.getPayments().stream()
                    .map(payment -> {
                        PaymentsEntity entity = modelMapper.map(payment, PaymentsEntity.class);
                        entity.setDebts(debts);
                        entity.setPaymentMethodEntity(payment.getPaymentMethodId() == null ? null
                                : paymentMethodService.findOwned(uuid, payment.getPaymentMethodId()));
                        return entity;
                    })
                    .toList();

            debts.setPayments(paymentsList);

            DebtCalculator.synchronize(debts);
            return mapperDto(debtsRespository.save(debts));

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);

        } catch (InvalidDataAccessResourceUsageException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_412, HttpStatus.PRECONDITION_FAILED,
                    Messages.DATA_ACCESS_SYNTAX);

        } catch (Exception e) {
            LoggerUtil.logError(e);
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
        DebtsEntity existingDebt = debtsRespository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        try {
            // Actualiza campos simples
            existingDebt.setCreditorName(request.getCreditorName());
            existingDebt.setTotalAmount(request.getTotalAmount());
            existingDebt.setCurrency(request.getCurrency());
            existingDebt.setHasInterest(request.getHasInterest());
            existingDebt.setNumberOfPayments(request.getNumberOfPayments());
            existingDebt.setDueDate(DateTimeUtils.parseToLocalDate(request.getDueDate()));
            existingDebt.setNotes(request.getNotes());

            // Limpiar alimentos anteriores
            existingDebt.getPayments().clear();

            // Crea nuevos Payments
            List<PaymentsEntity> newPayments = request.getPayments().stream()
                    .map(p -> {
                        PaymentsEntity pe = modelMapper.map(p, PaymentsEntity.class);
                        pe.setDebts(existingDebt);
                        pe.setPaymentMethodEntity(p.getPaymentMethodId() == null ? null
                                : paymentMethodService.findOwned(UUID.fromString(userId), p.getPaymentMethodId()));
                        return pe;
                    })
                    .toList();

            existingDebt.getPayments().addAll(newPayments);

            DebtCalculator.synchronize(existingDebt);
            return mapperDto(debtsRespository.save(existingDebt));

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);

        } catch (InvalidDataAccessResourceUsageException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_412, HttpStatus.PRECONDITION_FAILED,
                    Messages.DATA_ACCESS_SYNTAX);

        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        DebtsEntity debt = debtsRespository
                .findOne(filterWithParameters(Map.of(Filters.KEY_USER, userId, Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        debt.setErased(true);
        debtsRespository.save(debt);
        return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
    }

    private DebtsResponse mapperDto(DebtsEntity source) {
        DebtsResponse response = modelMapper.map(source, DebtsResponse.class);
        response.setPaymentsMade(DebtCalculator.paymentsMade(source));
        response.setIsFullyPaid(DebtCalculator.outstandingAmount(source).signum() == 0);
        response.setPaidAmount(DebtCalculator.paidAmount(source));
        response.setOutstandingAmount(DebtCalculator.outstandingAmount(source));
        response.setProgressPercentage(DebtCalculator.progressPercentage(source));
        if (response.getPayments() != null && source.getPayments() != null) {
            for (int i = 0; i < Math.min(response.getPayments().size(), source.getPayments().size()); i++) {
                PaymentsEntity payment = source.getPayments().get(i);
                response.getPayments().get(i).setPaymentMethodId(payment.getPaymentMethodEntity() == null ? null
                        : payment.getPaymentMethodEntity().getId());
            }
        }
        return response;
    }

    public Specification<DebtsEntity> filterWithParameters(Map<String, String> params) {
        return new DebtsSpec().getSpecificationByFilters(params);
    }

}
