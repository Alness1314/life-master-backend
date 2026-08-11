package com.alness.lifemaster.operations.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.assistance.repository.AssistanceRepository;
import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.exercises.entity.ExercisesEntity;
import com.alness.lifemaster.exercises.repository.ExercisesRepository;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.nutrition.entity.FoodEntity;
import com.alness.lifemaster.nutrition.entity.NutritionEntity;
import com.alness.lifemaster.nutrition.repository.NutritionRepository;
import com.alness.lifemaster.operations.report.dto.AssistanceReportResponse;
import com.alness.lifemaster.operations.report.dto.ConsolidatedReportResponse;
import com.alness.lifemaster.operations.report.dto.NutritionReportResponse;
import com.alness.lifemaster.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTests {
    @Mock private UserRepository userRepository;
    @Mock private AssistanceRepository assistanceRepository;
    @Mock private ExercisesRepository exercisesRepository;
    @Mock private NutritionRepository nutritionRepository;
    @Mock private ExpensesRepository expensesRepository;
    @Mock private IncomeRepository incomeRepository;
    @Mock private DebtsRespository debtsRepository;

    private final ReportPeriodResolver periodResolver = new ReportPeriodResolver();

    @InjectMocks private ReportsService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(userRepository.existsByIdAndErasedFalse(userId)).thenReturn(true);
        service = new ReportsService(periodResolver, userRepository, assistanceRepository, exercisesRepository,
                nutritionRepository, expensesRepository, incomeRepository, debtsRepository);
    }

    @Test
    void assistanceUsesRequestedUserAndCalculatesOvernightWork() {
        AssistanceEntity record = new AssistanceEntity();
        record.setId(UUID.randomUUID());
        record.setWorkDate(LocalDate.of(2026, 8, 11));
        record.setTimeEntry(LocalTime.of(22, 0));
        record.setDepartureTime(LocalTime.of(6, 0));
        record.setOnTime(true);
        record.setRetard(false);
        record.setJustifiedAbsence(false);
        record.setUnjustifiedAbsence(false);
        when(assistanceRepository.findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(
                eq(userId), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(record));

        AssistanceReportResponse report = service.assistance(
                userId, ReportPeriod.WEEKLY, LocalDate.of(2026, 8, 11));

        assertThat(report.records()).isEqualTo(1);
        assertThat(report.onTime()).isEqualTo(1);
        assertThat(report.workedMinutes()).isEqualTo(480);
        verify(assistanceRepository).findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(
                userId, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 16));
    }

    @Test
    void nutritionKeepsOptionalFoodsAndOnlySumsKnownCalories() {
        NutritionEntity emptyMeal = new NutritionEntity();
        emptyMeal.setId(UUID.randomUUID());
        emptyMeal.setDateTimeConsumption(LocalDate.of(2026, 8, 11).atTime(8, 0));
        emptyMeal.setName("Desayuno");
        emptyMeal.setFood(List.of());

        FoodEntity food = new FoodEntity();
        food.setFoodName("Fruta");
        food.setCalories(95);
        food.setQuantity("1");
        NutritionEntity meal = new NutritionEntity();
        meal.setId(UUID.randomUUID());
        meal.setDateTimeConsumption(LocalDate.of(2026, 8, 11).atTime(14, 0));
        meal.setName("Comida");
        meal.setFood(List.of(food));
        when(nutritionRepository
                .findAllByUserIdAndDateTimeConsumptionBetweenAndErasedFalseOrderByDateTimeConsumptionAsc(
                        eq(userId), any(), any())).thenReturn(List.of(emptyMeal, meal));

        NutritionReportResponse report = service.nutrition(
                userId, ReportPeriod.DAILY, LocalDate.of(2026, 8, 11));

        assertThat(report.meals()).isEqualTo(2);
        assertThat(report.foods()).isEqualTo(1);
        assertThat(report.totalKnownCalories()).isEqualTo(95);
        assertThat(report.items().get(0).foods()).isEmpty();
    }

    @Test
    void consolidatedSummarySeparatesCashFlowAndOperatingResult() {
        IncomeEntity income = new IncomeEntity();
        income.setAmount(new BigDecimal("1000.50"));
        income.setCurrency("MXN");
        ExpensesEntity paidExpense = expense("300.25", true);
        ExpensesEntity pendingExpense = expense("100.10", false);

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentDate(LocalDate.of(2026, 8, 10));
        payment.setAmountPaid(new BigDecimal("220.00"));
        payment.setPrincipalAmount(new BigDecimal("200.00"));
        payment.setInterestAmount(new BigDecimal("20.00"));
        payment.setIsPaid(true);
        DebtsEntity debt = new DebtsEntity();
        debt.setTotalAmount(new BigDecimal("1000.00"));
        debt.setCurrency("MXN");
        debt.setDisbursesFunds(true);
        debt.setReceivedDate(LocalDate.of(2026, 8, 5));
        debt.setReceivedAmount(new BigDecimal("1000.00"));
        debt.setPayments(List.of(payment));

        when(incomeRepository.findAllByUserIdAndPaymentDateBetweenAndErasedFalse(eq(userId), any(), any()))
                .thenReturn(List.of(income));
        when(expensesRepository.findAllByUserIdAndPaymentDateBetweenAndErasedFalse(eq(userId), any(), any()))
                .thenReturn(List.of(paidExpense, pendingExpense));
        when(debtsRepository.findAllByUserIdAndErasedFalse(userId)).thenReturn(List.of(debt));
        when(assistanceRepository.findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(eq(userId), any(), any()))
                .thenReturn(List.of());
        when(exercisesRepository.findAllByUserIdAndTrainingDateBetweenAndErasedFalseOrderByTrainingDateAsc(
                eq(userId), any(), any())).thenReturn(List.of());
        when(nutritionRepository
                .findAllByUserIdAndDateTimeConsumptionBetweenAndErasedFalseOrderByDateTimeConsumptionAsc(
                        eq(userId), any(), any())).thenReturn(List.of());

        ConsolidatedReportResponse report = service.summary(
                userId, ReportPeriod.MONTHLY, LocalDate.of(2026, 8, 11), "MXN");

        assertThat(report.financial().income()).isEqualByComparingTo("1000.50");
        assertThat(report.financial().expenses()).isEqualByComparingTo("400.35");
        assertThat(report.financial().pendingExpenses()).isEqualByComparingTo("100.10");
        assertThat(report.financial().operatingResult()).isEqualByComparingTo("680.25");
        assertThat(report.financial().netCashFlow()).isEqualByComparingTo("1480.25");
        assertThat(report.financial().outstandingDebt()).isEqualByComparingTo("800.00");
    }

    private ExpensesEntity expense(String amount, boolean paid) {
        ExpensesEntity expense = new ExpensesEntity();
        expense.setAmount(new BigDecimal(amount));
        expense.setCurrency("MXN");
        expense.setPaymentStatus(paid);
        return expense;
    }
}
