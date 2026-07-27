package com.alness.lifemaster.finance.summary;

import java.math.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.DebtCalculator;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.finance.budget.BudgetEntity;
import com.alness.lifemaster.finance.budget.BudgetRepository;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlySummaryService {
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final DebtsRespository debtsRepository;

    public MonthlySummaryResponse get(UUID userId, int year, int month, String currency) {
        if (year < 2000 || year > 2200 || month < 1 || month > 12) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Invalid summary period.");
        }
        if (currency == null || !currency.matches("^[A-Z]{3}$")) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Currency must use a three-letter ISO code.");
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<ExpensesEntity> expenses = expensesRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to).stream()
                .filter(value -> currency.equals(value.getCurrency())).toList();
        List<IncomeEntity> income = incomeRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to).stream()
                .filter(value -> currency.equals(value.getCurrency())).toList();
        List<BudgetEntity> budgets = budgetRepository
                .findAllByUserIdAndYearAndMonthAndErasedFalse(userId, year, month).stream()
                .filter(value -> currency.equals(value.getCurrency())).toList();
        List<DebtsEntity> debts = debtsRepository.findAllByUserIdAndErasedFalse(userId);
        debts = debts.stream().filter(value -> currency.equals(value.getCurrency())).toList();

        BigDecimal totalIncome = sumIncome(income);
        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal paidExpenses = expenses.stream().filter(e -> Boolean.TRUE.equals(e.getPaymentStatus()))
                .map(ExpensesEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingExpenses = totalExpenses.subtract(paidExpenses);
        Optional<BudgetEntity> generalBudget = budgets.stream().filter(b -> b.getCategory() == null).findFirst();
        BigDecimal budgeted = generalBudget.map(BudgetEntity::getAmount).orElseGet(() ->
                budgets.stream().map(BudgetEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal budgetUsage = budgeted.signum() == 0 ? BigDecimal.ZERO
                : totalExpenses.multiply(BigDecimal.valueOf(100))
                        .divide(budgeted, 2, RoundingMode.HALF_UP);
        BigDecimal totalDebt = debts.stream().map(DebtsEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingDebt = debts.stream().map(DebtCalculator::outstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<ExpensesEntity>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getId()));
        List<CategoryExpenseSummary> categories = grouped.values().stream().map(group -> {
            ExpensesEntity first = group.get(0);
            return new CategoryExpenseSummary(first.getCategory().getId(), first.getCategory().getName(),
                    sumExpenses(group));
        }).sorted(Comparator.comparing(CategoryExpenseSummary::amount).reversed()).toList();

        return new MonthlySummaryResponse(year, month, currency, totalIncome, totalExpenses, paidExpenses, pendingExpenses,
                totalIncome.subtract(totalExpenses), budgeted, budgetUsage, totalDebt, outstandingDebt, categories);
    }

    private BigDecimal sumExpenses(List<ExpensesEntity> values) {
        return values.stream().map(ExpensesEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumIncome(List<IncomeEntity> values) {
        return values.stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
