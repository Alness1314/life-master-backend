package com.alness.lifemaster.finance.budget;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {
    private final BudgetRepository repository;
    private final ExpensesRepository expensesRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> findPeriod(UUID userId, int year, int month) {
        validatePeriod(year, month);
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<ExpensesEntity> expenses = expensesRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to);
        return repository.findAllByUserIdAndYearAndMonthAndErasedFalse(userId, year, month)
                .stream().map(budget -> toResponse(budget, expenses))
                .sorted(Comparator.comparing(response -> response.categoryName() == null ? "" : response.categoryName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse findOne(UUID userId, UUID id) {
        BudgetEntity budget = findOwned(userId, id);
        LocalDate from = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return toResponse(budget, expensesRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to));
    }

    public BudgetResponse save(UUID userId, BudgetRequest request) {
        ensureUnique(userId, request, null);
        BudgetEntity entity = new BudgetEntity();
        entity.setUser(userRepository.findById(userId).orElseThrow(() -> notFound(userId)));
        apply(entity, request);
        return toResponse(repository.save(entity), List.of());
    }

    public BudgetResponse update(UUID userId, UUID id, BudgetRequest request) {
        BudgetEntity entity = findOwned(userId, id);
        boolean sameKey = Objects.equals(entity.getYear(), request.year())
                && Objects.equals(entity.getMonth(), request.month())
                && Objects.equals(entity.getCurrency(), request.currency())
                && Objects.equals(entity.getCategory() == null ? null : entity.getCategory().getId(), request.categoryId());
        if (!sameKey) {
            ensureUnique(userId, request, id);
        }
        apply(entity, request);
        return toResponse(repository.save(entity), List.of());
    }

    public void delete(UUID userId, UUID id) {
        BudgetEntity entity = findOwned(userId, id);
        entity.setErased(true);
        repository.save(entity);
    }

    private void apply(BudgetEntity entity, BudgetRequest request) {
        entity.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> notFound(request.categoryId())));
        entity.setYear(request.year());
        entity.setMonth(request.month());
        entity.setCurrency(request.currency());
        entity.setAmount(request.amount());
        entity.setAlertPercentage(request.alertPercentage());
    }

    private void ensureUnique(UUID userId, BudgetRequest request, UUID ignoredId) {
        boolean exists = request.categoryId() == null
                ? repository.existsByUserIdAndYearAndMonthAndCurrencyAndCategoryIsNullAndErasedFalse(
                        userId, request.year(), request.month(), request.currency())
                : repository.existsByUserIdAndYearAndMonthAndCurrencyAndCategoryIdAndErasedFalse(
                        userId, request.year(), request.month(), request.currency(), request.categoryId());
        if (exists) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    "A budget already exists for that period and category.");
        }
    }

    private BudgetEntity findOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndErasedFalse(id, userId).orElseThrow(() -> notFound(id));
    }

    private BudgetResponse toResponse(BudgetEntity budget, List<ExpensesEntity> expenses) {
        UUID categoryId = budget.getCategory() == null ? null : budget.getCategory().getId();
        BigDecimal spent = expenses.stream()
                .filter(expense -> budget.getCurrency().equals(expense.getCurrency()))
                .filter(expense -> categoryId == null || expense.getCategory().getId().equals(categoryId))
                .map(ExpensesEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal usage = spent.multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);
        return new BudgetResponse(budget.getId(), categoryId,
                budget.getCategory() == null ? null : budget.getCategory().getName(),
                budget.getYear(), budget.getMonth(), budget.getCurrency(), budget.getAmount(), spent, remaining, usage,
                budget.getAlertPercentage(), usage.compareTo(BigDecimal.valueOf(budget.getAlertPercentage())) >= 0,
                remaining.signum() < 0);
    }

    private void validatePeriod(int year, int month) {
        if (year < 2000 || year > 2200 || month < 1 || month > 12) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "Invalid budget period.");
        }
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Resource not found: " + id);
    }
}
