package com.alness.lifemaster.operations.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.assistance.repository.AssistanceRepository;
import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.DebtCalculator;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
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
import com.alness.lifemaster.operations.report.dto.DebtReportResponse;
import com.alness.lifemaster.operations.report.dto.ExerciseReportResponse;
import com.alness.lifemaster.operations.report.dto.ExpenseReportResponse;
import com.alness.lifemaster.operations.report.dto.IncomeReportResponse;
import com.alness.lifemaster.operations.report.dto.NutritionReportResponse;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsService {
    private static final Set<ReportPeriod> ASSISTANCE_PERIODS = Set.of(
            ReportPeriod.WEEKLY, ReportPeriod.FORTNIGHTLY, ReportPeriod.MONTHLY);
    private static final Set<ReportPeriod> DAILY_WEEKLY_MONTHLY = Set.of(
            ReportPeriod.DAILY, ReportPeriod.WEEKLY, ReportPeriod.MONTHLY);

    private final ReportPeriodResolver periodResolver;
    private final UserRepository userRepository;
    private final AssistanceRepository assistanceRepository;
    private final ExercisesRepository exercisesRepository;
    private final NutritionRepository nutritionRepository;
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;
    private final DebtsRespository debtsRepository;

    public AssistanceReportResponse assistance(UUID userId, ReportPeriod period, LocalDate referenceDate) {
        validateUser(userId);
        ReportRange range = periodResolver.resolveAllowed(period, referenceDate, ASSISTANCE_PERIODS);
        List<AssistanceEntity> records = assistanceRepository
                .findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, range.from(), range.to());
        List<AssistanceReportResponse.Item> items = records.stream().map(value -> {
            long minutes = workedMinutes(value.getTimeEntry(), value.getDepartureTime());
            return new AssistanceReportResponse.Item(value.getId(), value.getWorkDate(), value.getTimeEntry(),
                    value.getDepartureTime(), value.getOnTime(), value.getRetard(), value.getJustifiedAbsence(),
                    value.getUnjustifiedAbsence(), minutes);
        }).toList();
        return new AssistanceReportResponse(range, records.size(), countTrue(records, AssistanceEntity::getOnTime),
                countTrue(records, AssistanceEntity::getRetard),
                countTrue(records, AssistanceEntity::getJustifiedAbsence),
                countTrue(records, AssistanceEntity::getUnjustifiedAbsence),
                items.stream().mapToLong(AssistanceReportResponse.Item::workedMinutes).sum(), items);
    }

    public ExerciseReportResponse exercises(UUID userId, ReportPeriod period, LocalDate referenceDate) {
        validateUser(userId);
        ReportRange range = periodResolver.resolveAllowed(period, referenceDate, DAILY_WEEKLY_MONTHLY);
        List<ExercisesEntity> records = exercisesRepository
                .findAllByUserIdAndTrainingDateBetweenAndErasedFalseOrderByTrainingDateAsc(
                        userId, range.from(), range.to());
        long totalMinutes = records.stream().map(ExercisesEntity::getDurationMinutes)
                .filter(value -> value != null && value > 0).mapToLong(Integer::longValue).sum();
        BigDecimal average = records.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        Map<String, Long> byActivity = records.stream().collect(Collectors.groupingBy(
                value -> label(value.getActivityType(), "Sin especificar"), LinkedHashMap::new, Collectors.counting()));
        List<ExerciseReportResponse.Item> items = records.stream()
                .map(value -> new ExerciseReportResponse.Item(value.getId(), value.getTrainingDate(),
                        value.getStartTime(), value.getEndTime(), value.getActivityType(), value.getDurationMinutes(),
                        value.getNotes()))
                .toList();
        return new ExerciseReportResponse(range, records.size(), totalMinutes, average, byActivity, items);
    }

    public NutritionReportResponse nutrition(UUID userId, ReportPeriod period, LocalDate referenceDate) {
        validateUser(userId);
        ReportRange range = periodResolver.resolveAllowed(period, referenceDate, DAILY_WEEKLY_MONTHLY);
        List<NutritionEntity> records = nutritionRecords(userId, range);
        List<FoodEntity> foods = records.stream().flatMap(value -> safeFoods(value).stream()).toList();
        Map<String, Long> byType = records.stream().collect(Collectors.groupingBy(
                value -> label(value.getMealType(), "Sin especificar"), LinkedHashMap::new, Collectors.counting()));
        List<NutritionReportResponse.Item> items = records.stream().map(value ->
                new NutritionReportResponse.Item(value.getId(), value.getDateTimeConsumption(), value.getName(),
                        value.getMealType(), value.getNotes(), value.getPhoto() == null ? null : value.getPhoto().getId(),
                        safeFoods(value).stream().map(food -> new NutritionReportResponse.FoodItem(food.getId(),
                                food.getFoodName(), food.getCalories(), food.getQuantity(), food.getUnitMeasurement()))
                                .toList()))
                .toList();
        return new NutritionReportResponse(range, records.size(), foods.size(),
                foods.stream().filter(food -> food.getCalories() != null).count(),
                foods.stream().map(FoodEntity::getCalories).filter(value -> value != null)
                        .mapToLong(Integer::longValue).sum(),
                byType, items);
    }

    public ExpenseReportResponse expenses(UUID userId, ReportPeriod period, LocalDate referenceDate, String currency) {
        validateUser(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        ReportRange range = periodResolver.resolve(period, referenceDate);
        List<ExpensesEntity> records = expenseRecords(userId, range, normalizedCurrency);
        BigDecimal total = sum(records, ExpensesEntity::getAmount);
        BigDecimal paid = sum(records.stream().filter(value -> Boolean.TRUE.equals(value.getPaymentStatus())).toList(),
                ExpensesEntity::getAmount);
        Map<String, BigDecimal> byCategory = records.stream().collect(Collectors.groupingBy(
                value -> value.getCategory() == null ? "Sin categoría" : label(value.getCategory().getName(), "Sin categoría"),
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, ExpensesEntity::getAmount, BigDecimal::add)));
        List<ExpenseReportResponse.Item> items = records.stream().map(value -> new ExpenseReportResponse.Item(
                value.getId(), value.getPaymentDate(), value.getDescription(), value.getBankOrEntity(),
                value.getAmount(), value.getCurrency(), value.getPaymentStatus(),
                value.getCategory() == null ? null : value.getCategory().getId(),
                value.getCategory() == null ? null : value.getCategory().getName(),
                value.getAccount() == null ? null : value.getAccount().getId(),
                value.getPaymentMethod() == null ? null : value.getPaymentMethod().getId())).toList();
        return new ExpenseReportResponse(range, normalizedCurrency, records.size(), total, paid,
                total.subtract(paid), byCategory, items);
    }

    public IncomeReportResponse income(UUID userId, ReportPeriod period, LocalDate referenceDate, String currency) {
        validateUser(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        ReportRange range = periodResolver.resolve(period, referenceDate);
        List<IncomeEntity> records = incomeRecords(userId, range, normalizedCurrency);
        Map<String, BigDecimal> bySource = records.stream().collect(Collectors.groupingBy(
                value -> label(value.getSource(), "Sin especificar"), LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, IncomeEntity::getAmount, BigDecimal::add)));
        List<IncomeReportResponse.Item> items = records.stream().map(value -> new IncomeReportResponse.Item(
                value.getId(), value.getPaymentDate(), value.getSource(), value.getDescription(), value.getAmount(),
                value.getCurrency(), value.getAccount() == null ? null : value.getAccount().getId())).toList();
        return new IncomeReportResponse(range, normalizedCurrency, records.size(),
                sum(records, IncomeEntity::getAmount), bySource, items);
    }

    public DebtReportResponse debts(UUID userId, ReportPeriod period, LocalDate referenceDate, String currency) {
        validateUser(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        ReportRange range = periodResolver.resolve(period, referenceDate);
        List<DebtsEntity> debts = debtRecords(userId, normalizedCurrency);
        List<DebtReportResponse.Item> items = debts.stream().map(debt -> debtItem(debt, range)).toList();
        List<PaymentsEntity> periodPayments = debts.stream().flatMap(debt -> safePayments(debt).stream())
                .filter(payment -> inRange(payment.getPaymentDate(), range)).toList();
        List<PaymentsEntity> paidInPeriod = periodPayments.stream()
                .filter(payment -> Boolean.TRUE.equals(payment.getIsPaid())).toList();
        BigDecimal received = debts.stream()
                .filter(debt -> Boolean.TRUE.equals(debt.getDisbursesFunds()) && inRange(debt.getReceivedDate(), range))
                .map(debt -> zeroIfNull(debt.getReceivedAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DebtReportResponse(range, normalizedCurrency, debts.size(),
                items.stream().filter(DebtReportResponse.Item::overdue).count(),
                debts.stream().map(DebtsEntity::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                debts.stream().map(DebtCalculator::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                debts.stream().map(DebtCalculator::outstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                received,
                paidInPeriod.stream().map(PaymentsEntity::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add),
                paidInPeriod.stream().map(this::principal).reduce(BigDecimal.ZERO, BigDecimal::add),
                paidInPeriod.stream().map(payment -> zeroIfNull(payment.getInterestAmount()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                periodPayments.stream().filter(payment -> !Boolean.TRUE.equals(payment.getIsPaid()))
                        .map(PaymentsEntity::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add),
                items);
    }

    public ConsolidatedReportResponse summary(
            UUID userId, ReportPeriod period, LocalDate referenceDate, String currency) {
        validateUser(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        ReportRange range = periodResolver.resolve(period, referenceDate);
        List<ExpensesEntity> expenses = expenseRecords(userId, range, normalizedCurrency);
        List<IncomeEntity> income = incomeRecords(userId, range, normalizedCurrency);
        List<DebtsEntity> debts = debtRecords(userId, normalizedCurrency);
        List<PaymentsEntity> paidPayments = debts.stream().flatMap(debt -> safePayments(debt).stream())
                .filter(payment -> inRange(payment.getPaymentDate(), range))
                .filter(payment -> Boolean.TRUE.equals(payment.getIsPaid())).toList();
        BigDecimal totalIncome = sum(income, IncomeEntity::getAmount);
        BigDecimal totalExpenses = sum(expenses, ExpensesEntity::getAmount);
        BigDecimal paidExpenses = sum(
                expenses.stream().filter(value -> Boolean.TRUE.equals(value.getPaymentStatus())).toList(),
                ExpensesEntity::getAmount);
        BigDecimal debtProceeds = debts.stream()
                .filter(debt -> Boolean.TRUE.equals(debt.getDisbursesFunds()) && inRange(debt.getReceivedDate(), range))
                .map(debt -> zeroIfNull(debt.getReceivedAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debtPayments = paidPayments.stream().map(PaymentsEntity::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal principalPaid = paidPayments.stream().map(this::principal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interestPaid = paidPayments.stream().map(payment -> zeroIfNull(payment.getInterestAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstanding = debts.stream().map(DebtCalculator::outstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AssistanceEntity> assistance = assistanceRepository
                .findAllByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, range.from(), range.to());
        List<ExercisesEntity> exercises = exercisesRepository
                .findAllByUserIdAndTrainingDateBetweenAndErasedFalseOrderByTrainingDateAsc(
                        userId, range.from(), range.to());
        List<NutritionEntity> nutrition = nutritionRecords(userId, range);
        long knownCalories = nutrition.stream().flatMap(value -> safeFoods(value).stream())
                .map(FoodEntity::getCalories).filter(value -> value != null).mapToLong(Integer::longValue).sum();

        ConsolidatedReportResponse.Financial financial = new ConsolidatedReportResponse.Financial(
                totalIncome, totalExpenses, paidExpenses, totalExpenses.subtract(paidExpenses), debtProceeds,
                debtPayments, principalPaid, interestPaid, outstanding,
                totalIncome.subtract(paidExpenses).subtract(interestPaid),
                totalIncome.add(debtProceeds).subtract(paidExpenses).subtract(debtPayments));
        ConsolidatedReportResponse.Activity activity = new ConsolidatedReportResponse.Activity(
                assistance.size(), countTrue(assistance, AssistanceEntity::getRetard),
                countTrue(assistance, AssistanceEntity::getJustifiedAbsence)
                        + countTrue(assistance, AssistanceEntity::getUnjustifiedAbsence),
                exercises.size(), exercises.stream().map(ExercisesEntity::getDurationMinutes)
                        .filter(value -> value != null && value > 0).mapToLong(Integer::longValue).sum(),
                nutrition.size(), knownCalories);
        return new ConsolidatedReportResponse(range, normalizedCurrency, financial, activity);
    }

    private DebtReportResponse.Item debtItem(DebtsEntity debt, ReportRange range) {
        boolean overdue = !Boolean.TRUE.equals(debt.getIsFullyPaid()) && debt.getDueDate().isBefore(LocalDate.now());
        List<DebtReportResponse.PaymentItem> payments = safePayments(debt).stream()
                .filter(payment -> inRange(payment.getPaymentDate(), range))
                .map(payment -> new DebtReportResponse.PaymentItem(payment.getId(), payment.getPaymentDate(),
                        payment.getAmountPaid(), principal(payment), zeroIfNull(payment.getInterestAmount()),
                        payment.getIsPaid(), payment.getPaymentMethod(),
                        payment.getAccount() == null ? null : payment.getAccount().getId()))
                .toList();
        return new DebtReportResponse.Item(debt.getId(), debt.getCreditorName(), debt.getTotalAmount(),
                DebtCalculator.paidAmount(debt), DebtCalculator.outstandingAmount(debt),
                DebtCalculator.progressPercentage(debt), debt.getDueDate(), debt.getIsFullyPaid(), overdue,
                debt.getDisbursesFunds(), debt.getReceivedAmount(), debt.getReceivedDate(), payments);
    }

    private List<ExpensesEntity> expenseRecords(UUID userId, ReportRange range, String currency) {
        return expensesRepository.findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, range.from(), range.to())
                .stream().filter(value -> currency.equalsIgnoreCase(value.getCurrency())).toList();
    }

    private List<IncomeEntity> incomeRecords(UUID userId, ReportRange range, String currency) {
        return incomeRepository.findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, range.from(), range.to())
                .stream().filter(value -> currency.equalsIgnoreCase(value.getCurrency())).toList();
    }

    private List<DebtsEntity> debtRecords(UUID userId, String currency) {
        return debtsRepository.findAllByUserIdAndErasedFalse(userId).stream()
                .filter(value -> currency.equalsIgnoreCase(value.getCurrency())).toList();
    }

    private List<NutritionEntity> nutritionRecords(UUID userId, ReportRange range) {
        return nutritionRepository
                .findAllByUserIdAndDateTimeConsumptionBetweenAndErasedFalseOrderByDateTimeConsumptionAsc(
                        userId, range.from().atStartOfDay(), range.to().atTime(LocalTime.MAX));
    }

    private <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> extractor) {
        return values.stream().map(extractor).map(this::zeroIfNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countTrue(List<AssistanceEntity> values, Function<AssistanceEntity, Boolean> extractor) {
        return values.stream().filter(value -> Boolean.TRUE.equals(extractor.apply(value))).count();
    }

    private long workedMinutes(LocalTime entry, LocalTime departure) {
        if (entry == null || departure == null) {
            return 0;
        }
        Duration duration = Duration.between(entry, departure);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration.toMinutes();
    }

    private boolean inRange(LocalDate date, ReportRange range) {
        return date != null && !date.isBefore(range.from()) && !date.isAfter(range.to());
    }

    private BigDecimal principal(PaymentsEntity payment) {
        return payment.getPrincipalAmount() == null ? zeroIfNull(payment.getAmountPaid()) : payment.getPrincipalAmount();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<PaymentsEntity> safePayments(DebtsEntity debt) {
        return debt.getPayments() == null ? List.of() : debt.getPayments();
    }

    private List<FoodEntity> safeFoods(NutritionEntity nutrition) {
        return nutrition.getFood() == null ? List.of() : nutrition.getFood();
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency == null ? "MXN" : currency.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("^[A-Z]{3}$")) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "La moneda debe usar un código ISO de tres letras.");
        }
        return normalized;
    }

    private String label(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void validateUser(UUID userId) {
        if (!userRepository.existsByIdAndErasedFalse(userId)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
    }
}
