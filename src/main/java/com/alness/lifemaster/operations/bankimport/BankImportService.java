package com.alness.lifemaster.operations.bankimport;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.finance.account.*;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.income.repository.IncomeRepository;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BankImportService {
    private final BankImportRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialAccountRepository accountRepository;
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;
    private final BankStatementFileParser fileParser;

    @Value("${app.bank-import.max-size-bytes:20971520}")
    private long maxImportSize;

    public BankImportResponse importCsv(UUID userId, UUID accountId, UUID expenseCategoryId,
            MultipartFile file, boolean dryRun) {
        return importFile(userId, accountId, expenseCategoryId, "MXN", file, dryRun);
    }

    public BankImportResponse importFile(UUID userId, UUID accountId, UUID expenseCategoryId,
            String defaultCurrency, MultipartFile file, boolean dryRun) {
        byte[] content = read(file);
        String hash = sha256(content);
        if (!dryRun && repository.existsByUserIdAndFileHash(userId, hash)) {
            throw error(ApiCodes.API_CODE_409, HttpStatus.CONFLICT, "This bank file was already imported.");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> error(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "User not found."));
        FinancialAccountEntity account = accountId == null ? null
                : accountRepository.findByIdAndUserIdAndErasedFalse(accountId, userId)
                        .orElseThrow(() -> error(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Account not found."));
        ParsedBankStatement statement;
        try {
            statement = fileParser.parse(content, file.getOriginalFilename(), defaultCurrency);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<BankImportFailurePreview> failures = new ArrayList<>(statement.failures());
        List<BankMovement> rows = new ArrayList<>();
        for (BankMovement row : statement.movements()) {
            if (account != null && !account.getCurrency().equals(row.currency())) {
                failures.add(new BankImportFailurePreview(
                        row.rowNumber(),
                        row.description(),
                        "La moneda no coincide con la moneda de la cuenta seleccionada."));
            } else {
                rows.add(row);
            }
        }
        boolean hasExpenses = rows.stream().anyMatch(row -> row.type() == MovementType.EXPENSE);
        CategoryEntity category = expenseCategoryId == null ? null
                : categoryRepository.findById(expenseCategoryId)
                        .orElseThrow(() -> error(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Category not found."));
        if (hasExpenses && category == null) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "expenseCategoryId is required when the file contains expenses.");
        }
        int expenseCount = (int) rows.stream().filter(row -> row.type() == MovementType.EXPENSE).count();
        int incomeCount = rows.size() - expenseCount;
        if (!dryRun && !rows.isEmpty()) {
            for (BankMovement row : rows) {
                if (row.type() == MovementType.EXPENSE) {
                    saveExpense(user, account, category, row);
                } else {
                    saveIncome(user, account, row);
                }
            }
            BankImportEntity imported = new BankImportEntity();
            imported.setUser(user);
            String fileName = file.getOriginalFilename() == null ? "bank-import"
                    : file.getOriginalFilename().replace("\\", "/");
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            imported.setFileName(fileName.length() > 256 ? fileName.substring(fileName.length() - 256) : fileName);
            imported.setFileHash(hash);
            imported.setImportedRows(rows.size());
            imported.setImportedExpenses(expenseCount);
            imported.setImportedIncome(incomeCount);
            repository.save(imported);
        }
        List<BankImportMovementPreview> preview = rows.stream()
                .limit(200)
                .map(BankImportMovementPreview::from)
                .toList();
        List<BankImportFailurePreview> failurePreview = failures.stream()
                .limit(200)
                .toList();
        List<String> warnings = new ArrayList<>(statement.warnings());
        if (rows.size() > preview.size()) {
            warnings.add("La tabla muestra los primeros 200 movimientos exitosos.");
        }
        if (failures.size() > failurePreview.size()) {
            warnings.add("La tabla muestra los primeros 200 registros fallidos.");
        }
        int totalRows = statement.movements().size() + statement.failures().size();
        return new BankImportResponse(dryRun, statement.format(), totalRows,
                rows.size(), failures.size(), expenseCount, incomeCount,
                List.copyOf(warnings), preview, failurePreview);
    }

    private void saveExpense(UserEntity user, FinancialAccountEntity account, CategoryEntity category,
            BankMovement row) {
        ExpensesEntity value = new ExpensesEntity();
        value.setUser(user);
        value.setAccount(account);
        value.setCategory(category);
        value.setBankOrEntity(account == null ? "Bank import" : account.getName());
        value.setDescription(row.description());
        value.setAmount(row.amount());
        value.setCurrency(row.currency());
        value.setPaymentDate(row.date());
        value.setPaymentStatus(true);
        expensesRepository.save(value);
    }

    private void saveIncome(UserEntity user, FinancialAccountEntity account, BankMovement row) {
        IncomeEntity value = new IncomeEntity();
        value.setUser(user);
        value.setAccount(account);
        value.setSource(account == null ? "Bank import" : account.getName());
        value.setDescription(row.description());
        value.setAmount(row.amount());
        value.setCurrency(row.currency());
        value.setPaymentDate(row.date());
        incomeRepository.save(value);
    }

    private byte[] read(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > maxImportSize) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "El archivo no debe estar vacío ni exceder 20 MB.");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "No fue posible leer el archivo.");
        }
    }

    private String sha256(byte[] content) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private RestExceptionHandler error(String code, HttpStatus status, String message) {
        return new RestExceptionHandler(code, status, message);
    }
}
