package com.alness.lifemaster.operations.bankimport;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.csv.*;
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
    private static final long MAX_IMPORT_SIZE = 5L * 1024 * 1024;

    private final BankImportRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialAccountRepository accountRepository;
    private final ExpensesRepository expensesRepository;
    private final IncomeRepository incomeRepository;

    public BankImportResponse importCsv(UUID userId, UUID accountId, UUID expenseCategoryId,
            MultipartFile file, boolean dryRun) {
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
        List<Row> rows = parse(content);
        boolean hasExpenses = rows.stream().anyMatch(row -> row.type == Type.EXPENSE);
        CategoryEntity category = expenseCategoryId == null ? null
                : categoryRepository.findById(expenseCategoryId)
                        .orElseThrow(() -> error(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Category not found."));
        if (hasExpenses && category == null) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "expenseCategoryId is required when the file contains expenses.");
        }
        if (account != null && rows.stream().anyMatch(row -> !account.getCurrency().equals(row.currency))) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Every imported row must use the account currency.");
        }

        int expenseCount = (int) rows.stream().filter(row -> row.type == Type.EXPENSE).count();
        int incomeCount = rows.size() - expenseCount;
        if (!dryRun) {
            for (Row row : rows) {
                if (row.type == Type.EXPENSE) {
                    saveExpense(user, account, category, row);
                } else {
                    saveIncome(user, account, row);
                }
            }
            BankImportEntity imported = new BankImportEntity();
            imported.setUser(user);
            String fileName = file.getOriginalFilename() == null ? "bank.csv"
                    : file.getOriginalFilename().replace("\\", "/");
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            imported.setFileName(fileName.length() > 256 ? fileName.substring(fileName.length() - 256) : fileName);
            imported.setFileHash(hash);
            imported.setImportedRows(rows.size());
            imported.setImportedExpenses(expenseCount);
            imported.setImportedIncome(incomeCount);
            repository.save(imported);
        }
        return new BankImportResponse(dryRun, rows.size(), expenseCount, incomeCount, List.of());
    }

    private List<Row> parse(byte[] content) {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true).build().parse(reader)) {
            Set<String> required = Set.of("date", "description", "amount", "type", "currency");
            if (!parser.getHeaderMap().keySet().containsAll(required)) {
                throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                        "CSV headers required: date,description,amount,type,currency.");
            }
            List<Row> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                try {
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    if (amount.signum() <= 0) {
                        throw new IllegalArgumentException();
                    }
                    String currency = record.get("currency").toUpperCase(Locale.ROOT);
                    if (!currency.matches("^[A-Z]{3}$")) {
                        throw new IllegalArgumentException();
                    }
                    String description = record.get("description").trim();
                    if (description.isEmpty() || description.length() > 256) {
                        throw new IllegalArgumentException();
                    }
                    rows.add(new Row(LocalDate.parse(record.get("date")), description,
                            amount, Type.valueOf(record.get("type").toUpperCase(Locale.ROOT)), currency));
                } catch (RuntimeException exception) {
                    throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                            "Invalid data on CSV row " + record.getRecordNumber() + ".");
                }
            }
            if (rows.isEmpty()) {
                throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "The CSV file has no movements.");
            }
            return rows;
        } catch (IOException exception) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "Could not read CSV file.");
        }
    }

    private void saveExpense(UserEntity user, FinancialAccountEntity account, CategoryEntity category, Row row) {
        ExpensesEntity value = new ExpensesEntity();
        value.setUser(user);
        value.setAccount(account);
        value.setCategory(category);
        value.setBankOrEntity(account == null ? "Bank import" : account.getName());
        value.setDescription(row.description);
        value.setAmount(row.amount);
        value.setCurrency(row.currency);
        value.setPaymentDate(row.date);
        value.setPaymentStatus(true);
        expensesRepository.save(value);
    }

    private void saveIncome(UserEntity user, FinancialAccountEntity account, Row row) {
        IncomeEntity value = new IncomeEntity();
        value.setUser(user);
        value.setAccount(account);
        value.setSource(account == null ? "Bank import" : account.getName());
        value.setDescription(row.description);
        value.setAmount(row.amount);
        value.setCurrency(row.currency);
        value.setPaymentDate(row.date);
        incomeRepository.save(value);
    }

    private byte[] read(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_IMPORT_SIZE) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "CSV must not be empty or exceed 5 MB.");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw error(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, "Could not read CSV file.");
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

    private enum Type { EXPENSE, INCOME }
    private record Row(LocalDate date, String description, BigDecimal amount, Type type, String currency) {}
}
