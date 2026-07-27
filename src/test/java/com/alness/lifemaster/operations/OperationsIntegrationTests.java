package com.alness.lifemaster.operations;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.dto.response.ExpensesResponse;
import com.alness.lifemaster.expenses.service.ExpensesService;
import com.alness.lifemaster.operations.bankimport.*;
import com.alness.lifemaster.operations.receipt.*;
import com.alness.lifemaster.operations.report.FinancialReportService;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.operations.audit.*;
import com.alness.lifemaster.operations.reminder.*;
import com.alness.lifemaster.operations.alert.FinancialAlertService;

@SpringBootTest
@Transactional
class OperationsIntegrationTests {
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ExpensesService expensesService;
    @Autowired private ExpenseReceiptService receiptService;
    @Autowired private FinancialReportService reportService;
    @Autowired private BankImportService bankImportService;
    @Autowired private AuditEventService auditEventService;
    @Autowired private FinancialReminderService reminderService;
    @Autowired private FinancialAlertService alertService;

    @Test
    void storesAndDownloadsPrivateExpenseReceipt() throws Exception {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = category("Receipts");
        ExpensesResponse expense = expensesService.save(userId.toString(), expense(category, "Receipt expense"));
        MockMultipartFile file = new MockMultipartFile("file", "ticket.pdf", "application/pdf",
                "%PDF-test".getBytes(StandardCharsets.UTF_8));

        ExpenseReceiptResponse saved = receiptService.save(userId, expense.getId(), file);
        ExpenseReceiptEntity downloaded = receiptService.download(userId, saved.id());

        assertThat(downloaded.getOriginalName()).isEqualTo("ticket.pdf");
        assertThat(downloaded.getContent()).isEqualTo(file.getBytes());
    }

    @Test
    void previewsImportsAndRejectsSameConfirmedFileTwice() {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = category("Bank import");
        byte[] csv = ("date,description,amount,type,currency\n"
                + "2026-07-01,Salary,1000.00,INCOME,MXN\n"
                + "2026-07-02,Food,100.00,EXPENSE,MXN\n").getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "bank.csv", "text/csv", csv);

        BankImportResponse preview = bankImportService.importCsv(userId, null, category.getId(), file, true);
        BankImportResponse confirmed = bankImportService.importCsv(userId, null, category.getId(), file, false);

        assertThat(preview.dryRun()).isTrue();
        assertThat(confirmed.totalRows()).isEqualTo(2);
        assertThatThrownBy(() -> bankImportService.importCsv(userId, null, category.getId(), file, false))
                .isInstanceOf(RestExceptionHandler.class);
    }

    @Test
    void exportsMonthlyCsvWithUtf8BomAndMovements() {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = category("Reports");
        expensesService.save(userId.toString(), expense(category, "Exported expense"));

        byte[] report = reportService.monthlyCsv(userId, 2026, 7, "MXN");
        String content = new String(report, StandardCharsets.UTF_8);

        assertThat(report).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(content).contains("Exported expense", "EXPENSE");
    }

    @Test
    void recordsAuditAndCreatesOperationalReminder() {
        UUID userId = userRepository.findAll().get(0).getId();
        auditEventService.record(userId, "test@example.com", "POST", "/test", 201,
                "12345678-test", "127.0.0.1", 10);
        FinancialReminderResponse reminder = reminderService.save(userId,
                new FinancialReminderRequest("Pay card", "Payment is due",
                        java.time.LocalDateTime.now().plusDays(1)));

        assertThat(auditEventService.find(userId, 10)).isNotEmpty();
        assertThat(reminder.delivered()).isFalse();
        assertThat(alertService.refresh(userId)).isNotNull();
    }

    private CategoryEntity category(String name) {
        CategoryEntity value = new CategoryEntity();
        value.setName(name);
        value.setDescription("Test category");
        return categoryRepository.save(value);
    }

    private ExpensesRequest expense(CategoryEntity category, String description) {
        return ExpensesRequest.builder()
                .bankOrEntity("Cash")
                .description(description)
                .amount(new BigDecimal("125.50"))
                .category(category.getId().toString())
                .paymentDate("2026-07-15")
                .paymentStatus(true)
                .currency("MXN")
                .build();
    }
}
