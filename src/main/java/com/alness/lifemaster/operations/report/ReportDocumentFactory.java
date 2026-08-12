package com.alness.lifemaster.operations.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.alness.lifemaster.operations.report.dto.AssistanceReportResponse;
import com.alness.lifemaster.operations.report.dto.ConsolidatedReportResponse;
import com.alness.lifemaster.operations.report.dto.DebtReportResponse;
import com.alness.lifemaster.operations.report.dto.ExerciseReportResponse;
import com.alness.lifemaster.operations.report.dto.ExpenseReportResponse;
import com.alness.lifemaster.operations.report.dto.IncomeReportResponse;
import com.alness.lifemaster.operations.report.dto.NutritionReportResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportDocumentFactory {
    private final ReportsService reportsService;

    public ReportDocument create(
            UUID userId,
            ReportKind kind,
            ReportPeriod period,
            LocalDate referenceDate,
            String currency) {
        return switch (kind) {
            case SUMMARY -> summary(reportsService.summary(userId, period, referenceDate, currency));
            case ASSISTANCE -> assistance(reportsService.assistance(userId, period, referenceDate));
            case EXERCISES -> exercises(reportsService.exercises(userId, period, referenceDate));
            case NUTRITION -> nutrition(reportsService.nutrition(userId, period, referenceDate));
            case EXPENSES -> expenses(reportsService.expenses(userId, period, referenceDate, currency));
            case INCOME -> income(reportsService.income(userId, period, referenceDate, currency));
            case DEBTS -> debts(reportsService.debts(userId, period, referenceDate, currency));
        };
    }

    private ReportDocument summary(ConsolidatedReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Ingresos", report.financial().income());
        values.put("Gastos", report.financial().expenses());
        values.put("Flujo neto", report.financial().netCashFlow());
        values.put("Resultado operativo", report.financial().operatingResult());
        values.put("Deuda pendiente", report.financial().outstandingDebt());
        List<List<Object>> rows = new ArrayList<>();
        addMetric(rows, "Finanzas", "Gastos pagados", report.financial().paidExpenses());
        addMetric(rows, "Finanzas", "Gastos pendientes", report.financial().pendingExpenses());
        addMetric(rows, "Finanzas", "Entradas por deuda", report.financial().debtProceeds());
        addMetric(rows, "Finanzas", "Pagos de deuda", report.financial().debtPayments());
        addMetric(rows, "Finanzas", "Capital pagado", report.financial().debtPrincipalPaid());
        addMetric(rows, "Finanzas", "Intereses pagados", report.financial().debtInterestPaid());
        addMetric(rows, "Actividad", "Registros de asistencia", report.activity().assistanceRecords());
        addMetric(rows, "Actividad", "Retardos", report.activity().retards());
        addMetric(rows, "Actividad", "Ausencias", report.activity().absences());
        addMetric(rows, "Actividad", "Sesiones de ejercicio", report.activity().exerciseSessions());
        addMetric(rows, "Actividad", "Minutos de ejercicio", report.activity().exerciseMinutes());
        addMetric(rows, "Nutrición", "Comidas registradas", report.activity().nutritionMeals());
        addMetric(rows, "Nutrición", "Calorías conocidas", report.activity().knownCalories());
        return document("Resumen general", report.range(), report.currency(), values,
                List.of("Sección", "Indicador", "Valor"), rows);
    }

    private ReportDocument assistance(AssistanceReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Registros", report.records());
        values.put("A tiempo", report.onTime());
        values.put("Retardos", report.retards());
        values.put("Faltas justificadas", report.justifiedAbsences());
        values.put("Faltas sin justificar", report.unjustifiedAbsences());
        values.put("Minutos trabajados", report.workedMinutes());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.workDate(), nullable(item.timeEntry()), nullable(item.departureTime()),
                assistanceStatus(item), item.workedMinutes())).toList();
        return document("Reporte de asistencia", report.range(), null, values,
                List.of("Fecha", "Entrada", "Salida", "Estado", "Minutos trabajados"), rows);
    }

    private ReportDocument exercises(ExerciseReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Sesiones", report.sessions());
        values.put("Minutos totales", report.totalDurationMinutes());
        values.put("Promedio por sesión", report.averageDurationMinutes());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.trainingDate(), nullable(item.startTime()), nullable(item.endTime()),
                nullable(item.activityType()), nullable(item.durationMinutes()), nullable(item.notes()))).toList();
        return document("Reporte de ejercicio", report.range(), null, values,
                List.of("Fecha", "Inicio", "Fin", "Actividad", "Duración (min)", "Notas"), rows);
    }

    private ReportDocument nutrition(NutritionReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Comidas", report.meals());
        values.put("Alimentos detallados", report.foods());
        values.put("Alimentos con calorías", report.foodsWithCalories());
        values.put("Calorías conocidas", report.totalKnownCalories());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.consumedAt(), item.name(), nullable(item.mealType()),
                item.foods().stream().map(food -> food.name() + " (" + food.quantity()
                        + (food.unitMeasurement() == null ? "" : " " + food.unitMeasurement()) + ")")
                        .collect(Collectors.joining(", ")),
                item.foods().stream().mapToInt(food -> food.calories() == null ? 0 : food.calories()).sum(),
                nullable(item.notes()))).toList();
        return document("Reporte de nutrición", report.range(), null, values,
                List.of("Consumo", "Nombre", "Tipo de comida", "Alimentos", "Calorías", "Notas"), rows);
    }

    private ReportDocument expenses(ExpenseReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Movimientos", report.records());
        values.put("Gasto total", report.total());
        values.put("Pagado", report.paid());
        values.put("Pendiente", report.pending());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.paymentDate(), item.description(), nullable(item.category()), item.bankOrEntity(),
                Boolean.TRUE.equals(item.paid()) ? "Pagado" : "Pendiente", item.amount())).toList();
        return document("Reporte de gastos", report.range(), report.currency(), values,
                List.of("Fecha", "Descripción", "Categoría", "Entidad", "Estado", "Importe"), rows);
    }

    private ReportDocument income(IncomeReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Movimientos", report.records());
        values.put("Ingresos totales", report.total());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.paymentDate(), item.source(), item.description(), item.amount())).toList();
        return document("Reporte de ingresos", report.range(), report.currency(), values,
                List.of("Fecha", "Fuente", "Descripción", "Importe"), rows);
    }

    private ReportDocument debts(DebtReportResponse report) {
        Map<String, Object> values = linkedMap();
        values.put("Deudas", report.debts());
        values.put("Deudas vencidas", report.overdueDebts());
        values.put("Importe original", report.originalAmount());
        values.put("Capital pagado", report.paidPrincipal());
        values.put("Saldo pendiente", report.outstandingAmount());
        values.put("Pagado en el periodo", report.paidInPeriod());
        values.put("Intereses del periodo", report.interestPaidInPeriod());
        values.put("Pagos programados", report.scheduledInPeriod());
        List<List<Object>> rows = report.items().stream().map(item -> List.<Object>of(
                item.creditor(), item.dueDate(), item.totalAmount(), item.paidPrincipal(),
                item.outstandingAmount(), item.progressPercentage(), debtStatus(item))).toList();
        return document("Reporte de deudas", report.range(), report.currency(), values,
                List.of("Acreedor", "Vencimiento", "Importe original", "Capital pagado",
                        "Saldo pendiente", "Avance (%)", "Estado"), rows);
    }

    private ReportDocument document(String title, ReportRange range, String currency, Map<String, Object> summary,
            List<String> columns, List<List<Object>> rows) {
        return new ReportDocument(title, range, currency, summary, columns, rows);
    }

    private Map<String, Object> linkedMap() {
        return new LinkedHashMap<>();
    }

    private void addMetric(List<List<Object>> rows, String section, String label, Object value) {
        rows.add(List.of(section, label, value));
    }

    private Object nullable(Object value) {
        return value == null ? "" : value;
    }

    private String assistanceStatus(AssistanceReportResponse.Item item) {
        if (Boolean.TRUE.equals(item.unjustifiedAbsence())) return "Falta";
        if (Boolean.TRUE.equals(item.justifiedAbsence())) return "Falta justificada";
        if (Boolean.TRUE.equals(item.retard())) return "Retardo";
        return "A tiempo";
    }

    private String debtStatus(DebtReportResponse.Item item) {
        if (Boolean.TRUE.equals(item.fullyPaid())) return "Liquidada";
        if (item.overdue()) return "Vencida";
        return "Pendiente";
    }
}
