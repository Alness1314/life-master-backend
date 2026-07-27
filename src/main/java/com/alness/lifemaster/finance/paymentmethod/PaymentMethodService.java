package com.alness.lifemaster.finance.paymentmethod;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.finance.account.FinancialAccountService;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodService {
    private final PaymentMethodRepository repository;
    private final UserRepository userRepository;
    private final FinancialAccountService accountService;

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> findAll(UUID userId) {
        return repository.findAllByUserIdAndErasedFalseOrderByName(userId).stream().map(this::toResponse).toList();
    }

    public PaymentMethodResponse save(UUID userId, PaymentMethodRequest request) {
        ensureUniqueName(userId, request.name(), null);
        PaymentMethodEntity entity = new PaymentMethodEntity();
        entity.setUser(userRepository.findById(userId).orElseThrow(() -> notFound(userId)));
        apply(entity, userId, request);
        return toResponse(repository.save(entity));
    }

    public PaymentMethodResponse update(UUID userId, UUID id, PaymentMethodRequest request) {
        PaymentMethodEntity entity = findOwned(userId, id);
        ensureUniqueName(userId, request.name(), id);
        apply(entity, userId, request);
        return toResponse(repository.save(entity));
    }

    public void delete(UUID userId, UUID id) {
        PaymentMethodEntity entity = findOwned(userId, id);
        entity.setErased(true);
        entity.setActive(false);
        repository.save(entity);
    }

    public PaymentMethodEntity findOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndErasedFalse(id, userId).orElseThrow(() -> notFound(id));
    }

    private void apply(PaymentMethodEntity entity, UUID userId, PaymentMethodRequest request) {
        entity.setName(request.name().trim());
        entity.setMethodType(request.methodType());
        entity.setActive(request.active());
        entity.setAccount(request.accountId() == null ? null : accountService.findOwned(userId, request.accountId()));
    }

    private void ensureUniqueName(UUID userId, String name, UUID currentId) {
        repository.findByUserIdAndNameIgnoreCaseAndErasedFalse(userId, name.trim())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                            "A payment method with that name already exists.");
                });
    }

    private PaymentMethodResponse toResponse(PaymentMethodEntity entity) {
        return new PaymentMethodResponse(entity.getId(), entity.getName(), entity.getMethodType(),
                entity.getAccount() == null ? null : entity.getAccount().getId(), entity.getActive());
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                "Payment method not found: " + id);
    }
}
