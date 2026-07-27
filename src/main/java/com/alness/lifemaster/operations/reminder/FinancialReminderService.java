package com.alness.lifemaster.operations.reminder;

import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialReminderService {
    private final FinancialReminderRepository repository;
    private final UserRepository userRepository;

    public FinancialReminderResponse save(UUID userId, FinancialReminderRequest request) {
        FinancialReminderEntity value = new FinancialReminderEntity();
        value.setUser(userRepository.findById(userId).orElseThrow(() -> notFound(userId)));
        value.setTitle(request.title().trim()); value.setMessage(request.message().trim());
        value.setScheduledAt(request.scheduledAt()); value.setDelivered(false); value.setCancelled(false);
        return response(repository.save(value));
    }

    @Transactional(readOnly = true)
    public List<FinancialReminderResponse> findAll(UUID userId) {
        return repository.findAllByUserIdOrderByScheduledAtDesc(userId).stream().map(this::response).toList();
    }

    public FinancialReminderResponse cancel(UUID userId, UUID id) {
        FinancialReminderEntity value = repository.findByIdAndUserId(id, userId).orElseThrow(() -> notFound(id));
        if (Boolean.TRUE.equals(value.getDelivered())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    "A delivered reminder cannot be cancelled.");
        }
        value.setCancelled(true);
        return response(repository.save(value));
    }

    private FinancialReminderResponse response(FinancialReminderEntity value) {
        return new FinancialReminderResponse(value.getId(), value.getTitle(), value.getMessage(),
                value.getScheduledAt(), value.getDelivered(), value.getCancelled());
    }
    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Reminder not found: " + id);
    }
}
