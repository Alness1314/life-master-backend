package com.alness.lifemaster.finance.account;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialAccountService {
    private final FinancialAccountRepository repository;
    private final UserRepository userRepository;
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;

    @Transactional(readOnly = true)
    public List<FinancialAccountResponse> findAll(UUID userId) {
        return repository.findAllByUserIdAndErasedFalseOrderByName(userId).stream().map(this::toResponse).toList();
    }

    public FinancialAccountResponse save(UUID userId, FinancialAccountRequest request) {
        ensureUniqueName(userId, request.name(), null);
        FinancialAccountEntity entity = new FinancialAccountEntity();
        entity.setUser(findUser(userId));
        apply(entity, request);
        return toResponse(repository.save(entity));
    }

    public FinancialAccountResponse update(UUID userId, UUID id, FinancialAccountRequest request) {
        FinancialAccountEntity entity = findOwned(userId, id);
        ensureUniqueName(userId, request.name(), id);
        apply(entity, request);
        return toResponse(repository.save(entity));
    }

    public void delete(UUID userId, UUID id) {
        FinancialAccountEntity entity = findOwned(userId, id);
        entity.setErased(true);
        entity.setActive(false);
        repository.save(entity);
    }

    public FinancialAccountEntity findOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndErasedFalse(id, userId)
                .orElseThrow(() -> notFound(id));
    }

    private UserEntity findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private void apply(FinancialAccountEntity entity, FinancialAccountRequest request) {
        entity.setName(request.name().trim());
        entity.setAccountType(request.accountType());
        entity.setCurrency(request.currency());
        entity.setInitialBalance(request.initialBalance());
        entity.setActive(request.active());
    }

    private void ensureUniqueName(UUID userId, String name, UUID currentId) {
        repository.findByUserIdAndNameIgnoreCaseAndErasedFalse(userId, name.trim())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                            "An account with that name already exists.");
                });
    }

    private FinancialAccountResponse toResponse(FinancialAccountEntity entity) {
        java.math.BigDecimal income = incomeRepository.findAllByAccountIdAndErasedFalse(entity.getId()).stream()
                .map(IncomeEntity::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal expenses = expensesRepository.findAllByAccountIdAndErasedFalse(entity.getId()).stream()
                .map(ExpensesEntity::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return new FinancialAccountResponse(entity.getId(), entity.getName(), entity.getAccountType(),
                entity.getCurrency(), entity.getInitialBalance(),
                entity.getInitialBalance().add(income).subtract(expenses), entity.getActive());
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                "Financial account not found: " + id);
    }
}
