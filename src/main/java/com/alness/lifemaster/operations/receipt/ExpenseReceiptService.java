package com.alness.lifemaster.operations.receipt;

import java.io.IOException;
import java.util.*;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.expenses.repository.ExpensesRepository;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseReceiptService {
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/png", "image/jpeg");
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private final ExpenseReceiptRepository repository;
    private final ExpensesRepository expensesRepository;
    private final UserRepository userRepository;

    public ExpenseReceiptResponse save(UUID userId, UUID expenseId, MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_SIZE || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw badRequest("Receipt must be a PDF, PNG or JPEG of at most 10 MB.");
        }
        ExpensesEntity expense = expensesRepository.findByIdAndUserIdAndErasedFalse(expenseId, userId)
                .orElseThrow(() -> notFound(expenseId));
        try {
            ExpenseReceiptEntity entity = new ExpenseReceiptEntity();
            entity.setExpense(expense);
            entity.setUser(userRepository.getReferenceById(userId));
            entity.setOriginalName(safeName(file.getOriginalFilename()));
            entity.setContentType(file.getContentType());
            entity.setSizeBytes(file.getSize());
            entity.setContent(file.getBytes());
            return toResponse(repository.save(entity));
        } catch (IOException exception) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store receipt.");
        }
    }

    @Transactional(readOnly = true)
    public List<ExpenseReceiptResponse> findAll(UUID userId, UUID expenseId) {
        expensesRepository.findByIdAndUserIdAndErasedFalse(expenseId, userId)
                .orElseThrow(() -> notFound(expenseId));
        return repository.findAllByExpenseIdAndUserIdOrderByCreatedAtDesc(expenseId, userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseReceiptEntity download(UUID userId, UUID id) {
        return repository.findByIdAndUserId(id, userId).orElseThrow(() -> notFound(id));
    }

    public void delete(UUID userId, UUID expenseId, UUID id) {
        ExpenseReceiptEntity value = download(userId, id);
        if (!value.getExpense().getId().equals(expenseId)) {
            throw notFound(id);
        }
        repository.delete(value);
    }

    private ExpenseReceiptResponse toResponse(ExpenseReceiptEntity entity) {
        return new ExpenseReceiptResponse(entity.getId(), entity.getExpense().getId(), entity.getOriginalName(),
                entity.getContentType(), entity.getSizeBytes(), entity.getCreatedAt());
    }

    private String safeName(String name) {
        String value = name == null ? "receipt" : name.replace("\\", "/");
        value = value.substring(value.lastIndexOf('/') + 1);
        return value.length() > 256 ? value.substring(value.length() - 256) : value;
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Receipt not found: " + id);
    }

    private RestExceptionHandler badRequest(String message) {
        return new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, message);
    }
}
