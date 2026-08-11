package com.alness.lifemaster.operations.report;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.common.currency.CurrencyCode;
import com.alness.lifemaster.operations.report.dto.AssistanceReportResponse;
import com.alness.lifemaster.operations.report.dto.ConsolidatedReportResponse;
import com.alness.lifemaster.operations.report.dto.DebtReportResponse;
import com.alness.lifemaster.operations.report.dto.ExerciseReportResponse;
import com.alness.lifemaster.operations.report.dto.ExpenseReportResponse;
import com.alness.lifemaster.operations.report.dto.IncomeReportResponse;
import com.alness.lifemaster.operations.report.dto.NutritionReportResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reportes consolidados y por módulo del usuario autenticado.")
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/assistance")
    @Operation(summary = "Reporte de asistencia semanal, quincenal o mensual")
    public AssistanceReportResponse assistance(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return reportsService.assistance(userId, period, referenceDate);
    }

    @GetMapping("/exercises")
    @Operation(summary = "Reporte de ejercicio diario, semanal o mensual")
    public ExerciseReportResponse exercises(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return reportsService.exercises(userId, period, referenceDate);
    }

    @GetMapping("/nutrition")
    @Operation(summary = "Reporte de nutrición diario, semanal o mensual")
    public NutritionReportResponse nutrition(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return reportsService.nutrition(userId, period, referenceDate);
    }

    @GetMapping("/expenses")
    @Operation(summary = "Reporte de gastos por periodo y moneda")
    public ExpenseReportResponse expenses(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "MXN") CurrencyCode currency) {
        return reportsService.expenses(userId, period, referenceDate, currency.name());
    }

    @GetMapping("/income")
    @Operation(summary = "Reporte de ingresos por periodo y moneda")
    public IncomeReportResponse income(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "MXN") CurrencyCode currency) {
        return reportsService.income(userId, period, referenceDate, currency.name());
    }

    @GetMapping("/debts")
    @Operation(summary = "Reporte de deudas, saldos y pagos del periodo")
    public DebtReportResponse debts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "MXN") CurrencyCode currency) {
        return reportsService.debts(userId, period, referenceDate, currency.name());
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen financiero y de actividad consolidado")
    public ConsolidatedReportResponse summary(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "MXN") CurrencyCode currency) {
        return reportsService.summary(userId, period, referenceDate, currency.name());
    }
}
