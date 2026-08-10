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
import com.alness.lifemaster.files.*;
import com.alness.lifemaster.nutrition.dto.request.FoodRequest;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionResponse;
import com.alness.lifemaster.nutrition.service.NutritionService;

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
    @Autowired private StoredFileService storedFileService;
    @Autowired private NutritionService nutritionService;

    @Test
    void storesOptionalNutritionFoodsAndPhoto() {
        UUID userId = userRepository.findAll().get(0).getId();
        MockMultipartFile photo = new MockMultipartFile("photo", "meal.jpg", "image/jpeg",
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
        NutritionRequest create = NutritionRequest.builder()
                .dateTimeConsumption("2026-08-10 14:30:00")
                .mealType("LUNCH")
                .name("Comida familiar")
                .notes("Registro sin desglose")
                .food(java.util.List.of())
                .build();

        NutritionResponse saved = nutritionService.save(userId.toString(), create, photo);

        assertThat(saved.getFood()).isEmpty();
        assertThat(saved.getPhotoId()).isNotNull();
        assertThat(nutritionService.photo(userId.toString(), saved.getId().toString()).path()).exists();

        NutritionRequest update = NutritionRequest.builder()
                .dateTimeConsumption("2026-08-10 14:30:00")
                .mealType("LUNCH")
                .name("Comida familiar actualizada")
                .food(java.util.List.of(FoodRequest.builder()
                        .foodName("Ensalada")
                        .quantity("1")
                        .calories(null)
                        .unitMeasurement(null)
                        .build()))
                .removePhoto(true)
                .build();

        NutritionResponse updated = nutritionService.update(
                userId.toString(), saved.getId().toString(), update);

        assertThat(updated.getName()).isEqualTo("Comida familiar actualizada");
        assertThat(updated.getFood()).singleElement().satisfies(food -> {
            assertThat(food.getCalories()).isNull();
            assertThat(food.getUnitMeasurement()).isNull();
        });
        assertThat(updated.getPhotoId()).isNull();
    }

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
    void importsValidRowsAndReportsRejectedRows() {
        UUID userId = userRepository.findAll().get(0).getId();
        CategoryEntity category = category("Partial bank import");
        byte[] csv = ("date,description,amount,type,currency\n"
                + "2026-07-03,Valid income,500.00,INCOME,MXN\n"
                + "invalid-date,Rejected expense,100.00,EXPENSE,MXN\n").getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "partial-bank.csv", "text/csv", csv);

        BankImportResponse confirmed = bankImportService.importCsv(
                userId, null, category.getId(), file, false);

        assertThat(confirmed.totalRows()).isEqualTo(2);
        assertThat(confirmed.successfulRows()).isEqualTo(1);
        assertThat(confirmed.failedRows()).isEqualTo(1);
        assertThat(confirmed.movements()).singleElement()
                .satisfies(movement -> assertThat(movement.description()).isEqualTo("Valid income"));
        assertThat(confirmed.failures()).singleElement()
                .satisfies(failure -> {
                    assertThat(failure.rowNumber()).isEqualTo(3);
                    assertThat(failure.description()).isEqualTo("Rejected expense");
                    assertThat(failure.reason()).isEqualTo("Fecha inválida.");
                });
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

    @Test
    void storesPrivateGenericFileAndValidatesProfileImageType() {
        UUID userId = userRepository.findAll().get(0).getId();
        MockMultipartFile image = new MockMultipartFile("file", "avatar.png", "image/png",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 });

        StoredFileResponse saved = storedFileService.save(userId, FilePurpose.PROFILE_IMAGE, image);

        assertThat(saved.originalName()).isEqualTo("avatar.png");
        assertThat(saved.sha256()).hasSize(64);
        assertThat(storedFileService.content(userId, saved.id())).exists();
        assertThatThrownBy(() -> storedFileService.findOwned(UUID.randomUUID(), saved.id()))
                .isInstanceOf(RestExceptionHandler.class);
    }

    @Test
    void searchesSemanticAuditFields() {
        UUID userId = userRepository.findAll().get(0).getId();
        auditEventService.record(userId, "audit@example.com", "DELETE", "/api/v1/users/" + userId
                        + "/files/4f94fd16-22f8-4b16-b0ed-d46c61b77d22",
                "ELIMINAR", "FILES", "4f94fd16-22f8-4b16-b0ed-d46c61b77d22",
                "Eliminación de archivo", 204, "audit-test-1234", "127.0.0.1", "JUnit", 5);

        AuditEventPageResponse result = auditEventService.search(userId, null, "files", "eliminar",
                null, true, null, null, 0, 10);

        assertThat(result.totalElements()).isPositive();
        assertThat(result.content().get(0).module()).isEqualTo("FILES");
        assertThat(result.content().get(0).successful()).isTrue();
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
