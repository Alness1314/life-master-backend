package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.finance.account.FinancialAccountService;
import com.alness.lifemaster.finance.paymentmethod.PaymentMethodService;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecurringMovementService {
    private static final int MAX_GENERATED_PER_REQUEST = 1000;

    private final RecurringMovementRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialAccountService accountService;
    private final PaymentMethodService paymentMethodService;
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;

    @Transactional(readOnly = true)
    public List<RecurringMovementResponse> findAll(UUID userId) {
        return repository.findAllByUserIdAndErasedFalseOrderByNextExecutionDate(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecurringMovementResponse findOne(UUID userId, UUID id) {
        return toResponse(findOwned(userId, id));
    }

    public RecurringMovementResponse save(UUID userId, RecurringMovementRequest request) {
        RecurringMovementEntity entity = new RecurringMovementEntity();
        entity.setUser(userRepository.findById(userId).orElseThrow(() -> notFound(userId)));
        entity.setNextExecutionDate(request.startDate());
        apply(entity, userId, request, false);
        return toResponse(repository.save(entity));
    }

    public RecurringMovementResponse update(UUID userId, UUID id, RecurringMovementRequest request) {
        RecurringMovementEntity entity = findOwned(userId, id);
        apply(entity, userId, request, true);
        return toResponse(repository.save(entity));
    }

    public void delete(UUID userId, UUID id) {
        RecurringMovementEntity entity = findOwned(userId, id);
        entity.setErased(true);
        entity.setActive(false);
        repository.save(entity);
    }

    public RecurringGenerationResponse generateDue(UUID userId, LocalDate through) {
        if (through.isAfter(LocalDate.now().plusYears(2))) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Recurring movements can only be generated up to two years ahead.");
        }
        int expenses = 0;
        int income = 0;
        int total = 0;
        for (RecurringMovementEntity recurring : repository
                .findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(userId, through)) {
            while (!recurring.getNextExecutionDate().isAfter(through)
                    && (recurring.getEndDate() == null
                            || !recurring.getNextExecutionDate().isAfter(recurring.getEndDate()))) {
                if (++total > MAX_GENERATED_PER_REQUEST) {
                    throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                            "Generation limit exceeded; use a shorter period.");
                }
                if (recurring.getMovementType() == MovementType.EXPENSE) {
                    createExpense(recurring);
                    expenses++;
                } else {
                    createIncome(recurring);
                    income++;
                }
                recurring.setNextExecutionDate(recurring.getFrequency()
                        .next(recurring.getNextExecutionDate(), recurring.getStartDate()));
            }
            if (recurring.getEndDate() != null
                    && recurring.getNextExecutionDate().isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
            }
            repository.save(recurring);
        }
        return new RecurringGenerationResponse(through, expenses, income);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RecurringGenerationResponse generateOneDue(UUID recurringId, LocalDate through) {
        RecurringMovementEntity recurring = repository.findActiveByIdForUpdate(recurringId)
                .orElse(null);
        if (recurring == null || recurring.getNextExecutionDate().isAfter(through)) {
            return new RecurringGenerationResponse(through, 0, 0);
        }

        int expenses = 0;
        int income = 0;
        int generated = 0;
        while (!recurring.getNextExecutionDate().isAfter(through)
                && (recurring.getEndDate() == null
                        || !recurring.getNextExecutionDate().isAfter(recurring.getEndDate()))) {
            if (++generated > MAX_GENERATED_PER_REQUEST) {
                throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                        "Se excedió el límite de movimientos generados; revisa la configuración recurrente.");
            }
            if (recurring.getMovementType() == MovementType.EXPENSE) {
                createExpense(recurring);
                expenses++;
            } else {
                createIncome(recurring);
                income++;
            }
            recurring.setNextExecutionDate(recurring.getFrequency()
                    .next(recurring.getNextExecutionDate(), recurring.getStartDate()));
        }
        if (recurring.getEndDate() != null
                && recurring.getNextExecutionDate().isAfter(recurring.getEndDate())) {
            recurring.setActive(false);
        }
        repository.save(recurring);
        return new RecurringGenerationResponse(through, expenses, income);
    }

    private void createExpense(RecurringMovementEntity recurring) {
        ExpensesEntity expense = new ExpensesEntity();
        expense.setUser(recurring.getUser());
        expense.setCategory(recurring.getCategory());
        expense.setAccount(recurring.getAccount());
        expense.setPaymentMethod(recurring.getPaymentMethod());
        expense.setBankOrEntity(recurring.getAccount() == null ? "Recurring"
                : recurring.getAccount().getName());
        expense.setDescription(recurring.getDescription());
        expense.setAmount(recurring.getAmount());
        expense.setCurrency(recurring.getCurrency());
        expense.setPaymentDate(recurring.getNextExecutionDate());
        expense.setPaymentStatus(true);
        expensesRepository.save(expense);
    }

    private void createIncome(RecurringMovementEntity recurring) {
        IncomeEntity income = new IncomeEntity();
        income.setUser(recurring.getUser());
        income.setAccount(recurring.getAccount());
        income.setSource(recurring.getAccount() == null ? "Recurring"
                : recurring.getAccount().getName());
        income.setDescription(recurring.getDescription());
        income.setAmount(recurring.getAmount());
        income.setCurrency(recurring.getCurrency());
        income.setPaymentDate(recurring.getNextExecutionDate());
        incomeRepository.save(income);
    }

    private void apply(RecurringMovementEntity entity, UUID userId, RecurringMovementRequest request,
            boolean update) {
        entity.setMovementType(request.movementType());
        entity.setDescription(request.description().trim());
        entity.setAmount(request.amount());
        entity.setCurrency(request.currency());
        entity.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> notFound(request.categoryId())));
        entity.setAccount(request.accountId() == null ? null : accountService.findOwned(userId, request.accountId()));
        entity.setPaymentMethod(request.paymentMethodId() == null ? null
                : paymentMethodService.findOwned(userId, request.paymentMethodId()));
        if (entity.getAccount() != null && !entity.getAccount().getCurrency().equals(request.currency())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Recurring movement currency must match the account currency.");
        }
        if (entity.getPaymentMethod() != null && entity.getPaymentMethod().getAccount() != null
                && entity.getAccount() != null
                && !entity.getPaymentMethod().getAccount().getId().equals(entity.getAccount().getId())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Payment method does not belong to the selected account.");
        }
        entity.setFrequency(request.frequency());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setActive(request.active());
        if (update && entity.getNextExecutionDate().isBefore(request.startDate())) {
            entity.setNextExecutionDate(request.startDate());
        }
    }

    private RecurringMovementEntity findOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndErasedFalse(id, userId).orElseThrow(() -> notFound(id));
    }

    private RecurringMovementResponse toResponse(RecurringMovementEntity entity) {
        return new RecurringMovementResponse(entity.getId(), entity.getMovementType(), entity.getDescription(),
                entity.getAmount(), entity.getCurrency(),
                entity.getCategory() == null ? null : entity.getCategory().getId(),
                entity.getCategory() == null ? null : entity.getCategory().getName(),
                entity.getAccount() == null ? null : entity.getAccount().getId(),
                entity.getAccount() == null ? null : entity.getAccount().getName(),
                entity.getPaymentMethod() == null ? null : entity.getPaymentMethod().getId(),
                entity.getPaymentMethod() == null ? null : entity.getPaymentMethod().getName(),
                entity.getFrequency(), entity.getStartDate(), entity.getEndDate(), entity.getNextExecutionDate(),
                entity.getActive());
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                "Recurring movement resource not found: " + id);
    }
}
