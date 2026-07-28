package com.alness.lifemaster.operations.alert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.DebtCalculator;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.finance.budget.*;
import com.alness.lifemaster.finance.recurring.RecurringMovementRepository;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialAlertService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FinancialAlertRepository repository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;
    private final DebtsRespository debtsRepository;
    private final RecurringMovementRepository recurringRepository;

    @Transactional(readOnly = true)
    public List<FinancialAlertResponse> findAll(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();
    }

    public List<FinancialAlertResponse> refresh(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> notFound(userId));
        LocalDate now = LocalDate.now();
        for (BudgetResponse budget : budgetService.findPeriod(userId, now.getYear(), now.getMonthValue())) {
            if (budget.alert()) {
                create(user, "BUDGET", budget.exceeded() ? "HIGH" : "MEDIUM",
                        budget.exceeded() ? "Presupuesto excedido" : "Alerta de presupuesto",
                        "El presupuesto ha alcanzado un " + budget.usagePercentage() + "% de uso para "
                                + (budget.categoryName() == null ? "el mes actual" : budget.categoryName()) + ".",
                        "budget:" + budget.id() + ":" + budget.spent());
            }
        }
        debtsRepository.findAllByUserIdAndErasedFalse(userId).stream()
                .filter(debt -> DebtCalculator.outstandingAmount(debt).signum() > 0)
                .filter(debt -> !debt.getDueDate().isAfter(now.plusDays(7)))
                .forEach(debt -> create(user, "DEBT", debt.getDueDate().isBefore(now) ? "HIGH" : "MEDIUM",
                        debt.getDueDate().isBefore(now) ? "Deuda vencida" : "Deuda próxima a vencer",
                        "La deuda con " + debt.getCreditorName()
                                + (debt.getDueDate().isBefore(now) ? " venció el " : " vence el ")
                                + debt.getDueDate().format(DATE_FORMAT) + " y tiene un saldo pendiente de "
                                + DebtCalculator.outstandingAmount(debt) + " " + debt.getCurrency() + ".",
                        "debt:" + debt.getId() + ":" + debt.getDueDate()));
        recurringRepository
                .findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(userId, now.plusDays(3))
                .forEach(value -> create(user, "RECURRING", "LOW", "Movimiento recurrente próximo",
                        value.getDescription() + " está programado para el "
                                + value.getNextExecutionDate().format(DATE_FORMAT) + ".",
                        "recurring:" + value.getId() + ":" + value.getNextExecutionDate()));
        return findAll(userId);
    }

    public FinancialAlertResponse markRead(UUID userId, UUID id) {
        FinancialAlertEntity value = repository.findByIdAndUserId(id, userId).orElseThrow(() -> notFound(id));
        value.setRead(true);
        return response(repository.save(value));
    }

    private void create(UserEntity user, String type, String severity, String title, String message, String key) {
        FinancialAlertEntity value = repository.findByUserIdAndReferenceKey(user.getId(), key)
                .orElseGet(() -> {
                    FinancialAlertEntity alert = new FinancialAlertEntity();
                    alert.setUser(user);
                    alert.setReferenceKey(key);
                    alert.setRead(false);
                    return alert;
                });
        value.setAlertType(type);
        value.setSeverity(severity);
        value.setTitle(title);
        value.setMessage(message);
        repository.save(value);
    }

    private FinancialAlertResponse response(FinancialAlertEntity value) {
        return new FinancialAlertResponse(value.getId(), value.getAlertType(), value.getSeverity(), value.getTitle(),
                value.getMessage(), value.getRead(), value.getCreatedAt());
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Alert resource not found: " + id);
    }
}
