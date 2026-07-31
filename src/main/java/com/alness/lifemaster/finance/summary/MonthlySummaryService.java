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
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.finance.account.FinancialAccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlySummaryService {
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;
    private final DebtsRespository debtsRepository;
    private final FinancialAccountService financialAccountService;

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
        List<DebtsEntity> debts = debtsRepository.findAllByUserIdAndErasedFalse(userId);
        debts = debts.stream().filter(value -> currency.equals(value.getCurrency())).toList();

        BigDecimal totalIncome = sumIncome(income);
        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal paidExpenses = expenses.stream().filter(e -> Boolean.TRUE.equals(e.getPaymentStatus()))
                .map(ExpensesEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingExpenses = totalExpenses.subtract(paidExpenses);
        BigDecimal totalDebt = debts.stream().map(DebtsEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingDebt = debts.stream().map(DebtCalculator::outstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debtProceeds = debts.stream()
                .filter(value -> Boolean.TRUE.equals(value.getDisbursesFunds()))
                .filter(value -> value.getReceivedDate() != null
                        && !value.getReceivedDate().isBefore(from)
                        && !value.getReceivedDate().isAfter(to))
                .map(value -> value.getReceivedAmount() == null ? BigDecimal.ZERO : value.getReceivedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var periodPayments = debts.stream().flatMap(value -> value.getPayments().stream())
                .filter(value -> !value.getPaymentDate().isBefore(from) && !value.getPaymentDate().isAfter(to))
                .toList();
        BigDecimal debtPayments = periodPayments.stream()
                .filter(value -> Boolean.TRUE.equals(value.getIsPaid()))
                .map(value -> value.getAmountPaid())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debtPrincipalPaid = periodPayments.stream()
                .filter(value -> Boolean.TRUE.equals(value.getIsPaid()))
                .map(value -> value.getPrincipalAmount() == null ? value.getAmountPaid() : value.getPrincipalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debtInterestPaid = periodPayments.stream()
                .filter(value -> Boolean.TRUE.equals(value.getIsPaid()))
                .map(value -> value.getInterestAmount() == null ? BigDecimal.ZERO : value.getInterestAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal scheduledDebtPayments = periodPayments.stream()
                .filter(value -> !Boolean.TRUE.equals(value.getIsPaid()))
                .map(value -> value.getAmountPaid())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal operatingBalance = totalIncome.subtract(paidExpenses).subtract(debtInterestPaid);
        BigDecimal cashFlowBalance = totalIncome.add(debtProceeds).subtract(paidExpenses).subtract(debtPayments);
        BigDecimal freeMargin = cashFlowBalance.subtract(scheduledDebtPayments);
        BigDecimal availableAccountBalance = financialAccountService.findAll(userId).stream()
                .filter(account -> currency.equals(account.currency()))
                .map(account -> account.currentBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<ExpensesEntity>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getId()));
        List<CategoryExpenseSummary> categories = grouped.values().stream().map(group -> {
            ExpensesEntity first = group.get(0);
            return new CategoryExpenseSummary(first.getCategory().getId(), first.getCategory().getName(),
                    sumExpenses(group));
        }).sorted(Comparator.comparing(CategoryExpenseSummary::amount).reversed()).toList();

        return new MonthlySummaryResponse(year, month, currency, totalIncome, totalExpenses, paidExpenses, pendingExpenses,
                cashFlowBalance, operatingBalance, debtProceeds, debtPayments, debtPrincipalPaid, debtInterestPaid,
                scheduledDebtPayments, freeMargin, availableAccountBalance,
                totalDebt, outstandingDebt, categories);
    }

    private BigDecimal sumExpenses(List<ExpensesEntity> values) {
        return values.stream().map(ExpensesEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumIncome(List<IncomeEntity> values) {
        return values.stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
