package com.alness.lifemaster.operations.report;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialReportService {
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;

    public byte[] monthlyCsv(UUID userId, int year, int month, String currency) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<ExpensesEntity> expenses = expensesRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to).stream()
                .filter(value -> currency.equals(value.getCurrency())).toList();
        List<IncomeEntity> income = incomeRepository
                .findAllByUserIdAndPaymentDateBetweenAndErasedFalse(userId, from, to).stream()
                .filter(value -> currency.equals(value.getCurrency())).toList();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(0xEF);
            output.write(0xBB);
            output.write(0xBF);
            try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
                    CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                            .setHeader("type", "date", "description", "category_or_source", "amount", "currency",
                                    "paid", "account_id", "payment_method_id")
                            .build())) {
                for (IncomeEntity value : income) {
                    csv.printRecord("INCOME", value.getPaymentDate(), safeCell(value.getDescription()),
                            safeCell(value.getSource()),
                            decimal(value.getAmount()), value.getCurrency(), true,
                            value.getAccount() == null ? "" : value.getAccount().getId(), "");
                }
                for (ExpensesEntity value : expenses) {
                    csv.printRecord("EXPENSE", value.getPaymentDate(), safeCell(value.getDescription()),
                            safeCell(value.getCategory().getName()), decimal(value.getAmount()), value.getCurrency(),
                            value.getPaymentStatus(), value.getAccount() == null ? "" : value.getAccount().getId(),
                            value.getPaymentMethod() == null ? "" : value.getPaymentMethod().getId());
                }
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate CSV report", exception);
        }
    }

    private String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String safeCell(String value) {
        return value != null && !value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    }
}
