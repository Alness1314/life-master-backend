package com.alness.lifemaster.finance;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.service.ExpensesService;
import com.alness.lifemaster.finance.account.*;
import com.alness.lifemaster.finance.budget.*;
import com.alness.lifemaster.finance.summary.MonthlySummaryResponse;
import com.alness.lifemaster.finance.summary.MonthlySummaryService;
import com.alness.lifemaster.finance.recurring.*;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.service.IncomeService;
import com.alness.lifemaster.users.repository.UserRepository;

@SpringBootTest
@Transactional
class FinanceCoreIntegrationTests {
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private FinancialAccountService accountService;
    @Autowired private ExpensesService expensesService;
    @Autowired private IncomeService incomeService;
    @Autowired private BudgetService budgetService;
    @Autowired private MonthlySummaryService summaryService;
    @Autowired private RecurringMovementService recurringService;
    @Autowired private ExpensesRepository expensesRepository;

    @Test
    void calculatesAccountBudgetAndMonthlySummaryFromRealMovements() {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = new CategoryEntity();
        category.setName("Food test");
        category.setDescription("Test category");
        category = categoryRepository.save(category);

        FinancialAccountResponse account = accountService.save(userId,
                new FinancialAccountRequest("Main account", AccountType.CHECKING, "MXN",
                        new BigDecimal("1000.00"), true));

        ExpensesRequest expense = ExpensesRequest.builder()
                .bankOrEntity("Main account")
                .description("Groceries")
                .amount(new BigDecimal("300.00"))
                .category(category.getId().toString())
                .paymentDate("2026-07-15")
                .paymentStatus(true)
                .accountId(account.id())
                .build();
        expensesService.save(userId.toString(), expense);

        IncomeRequest income = new IncomeRequest();
        income.setSource("Salary");
        income.setDescription("Monthly salary");
        income.setAmount(new BigDecimal("2000.00"));
        income.setPaymentDate("2026-07-10");
        income.setAccountId(account.id());
        incomeService.save(userId.toString(), income);

        budgetService.save(userId, new BudgetRequest(category.getId(), 2026, 7, "MXN",
                new BigDecimal("500.00"), 80));

        MonthlySummaryResponse summary = summaryService.get(userId, 2026, 7, "MXN");
        FinancialAccountResponse updatedAccount = accountService.findAll(userId).get(0);
        BudgetResponse budget = budgetService.findPeriod(userId, 2026, 7).get(0);

        assertThat(summary.totalIncome()).isEqualByComparingTo("2000.00");
        assertThat(summary.totalExpenses()).isEqualByComparingTo("300.00");
        assertThat(summary.netBalance()).isEqualByComparingTo("1700.00");
        assertThat(updatedAccount.currentBalance()).isEqualByComparingTo("2700.00");
        assertThat(budget.spent()).isEqualByComparingTo("300.00");
        assertThat(budget.remaining()).isEqualByComparingTo("200.00");
    }

    @Test
    void generatesEachRecurringOccurrenceOnlyOnce() {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = new CategoryEntity();
        category.setName("Recurring category");
        category.setDescription("Test category");
        category = categoryRepository.save(category);

        recurringService.save(userId, new RecurringMovementRequest(
                MovementType.EXPENSE, "Subscription", new BigDecimal("99.00"), "MXN",
                category.getId(), null, null, RecurrenceFrequency.MONTHLY,
                java.time.LocalDate.of(2026, 7, 1), null, true));

        RecurringGenerationResponse first = recurringService.generateDue(userId,
                java.time.LocalDate.of(2026, 7, 1));
        RecurringGenerationResponse second = recurringService.generateDue(userId,
                java.time.LocalDate.of(2026, 7, 1));

        assertThat(first.generatedExpenses()).isEqualTo(1);
        assertThat(second.generatedExpenses()).isZero();
        assertThat(expensesRepository.findAllByUserIdAndPaymentDateBetweenAndErasedFalse(
                userId, java.time.LocalDate.of(2026, 7, 1), java.time.LocalDate.of(2026, 7, 1)))
                .hasSize(1);
    }
}
