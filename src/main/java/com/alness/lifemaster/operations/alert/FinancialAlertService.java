package com.alness.lifemaster.operations.alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.*;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.DebtCalculator;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.finance.recurring.RecurringMovementRepository;
import com.alness.lifemaster.sender.dto.EmailRequest;
import com.alness.lifemaster.sender.dto.ImageDto;
import com.alness.lifemaster.sender.service.EmailService;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FinancialAlertService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FinancialAlertRepository repository;
    private final UserRepository userRepository;
    private final DebtsRespository debtsRepository;
    private final RecurringMovementRepository recurringRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<FinancialAlertResponse> findAll(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(value -> !"BUDGET".equalsIgnoreCase(value.getAlertType()))
                .map(this::response)
                .toList();
    }

    public List<FinancialAlertResponse> refresh(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> notFound(userId));
        LocalDate now = LocalDate.now();
        debtsRepository.findAllByUserIdAndErasedFalse(userId).stream()
                .filter(debt -> DebtCalculator.outstandingAmount(debt).signum() > 0)
                .filter(debt -> !debt.getDueDate().isAfter(now.plusDays(7)))
                .forEach(debt -> create(user, "DEBT", debt.getDueDate().isBefore(now) ? "HIGH" : "MEDIUM",
                        debt.getDueDate().isBefore(now) ? "Deuda vencida" : "Deuda próxima a vencer",
                        "La deuda con " + debt.getCreditorName()
                                + (debt.getDueDate().isBefore(now) ? " venció el " : " vence el ")
                                + debt.getDueDate().format(DATE_FORMAT) + " y tiene un saldo pendiente de "
                                + formatMoney(DebtCalculator.outstandingAmount(debt), debt.getCurrency()) + ".",
                        "debt:" + debt.getId() + ":" + debt.getDueDate()));
        recurringRepository
                .findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(userId, now.plusDays(3))
                .forEach(value -> create(user, "RECURRING", "LOW", "Movimiento recurrente próximo",
                        value.getDescription() + " está programado para el "
                                + value.getNextExecutionDate().format(DATE_FORMAT) + ".",
                        "recurring:" + value.getId() + ":" + value.getNextExecutionDate()));
        return findAll(userId);
    }

    public FinancialAlertResponse markRead(UUID userId, UUID id) {
        FinancialAlertEntity value = repository.findByIdAndUserId(id, userId).orElseThrow(() -> notFound(id));
        value.setRead(true);
        return response(repository.save(value));
    }

    public void markAllRead(UUID userId) {
        if (!userRepository.existsByIdAndErasedFalse(userId)) {
            throw notFound(userId);
        }
        repository.markAllReadByUserId(userId);
    }

    private void create(UserEntity user, String type, String severity, String title, String message, String key) {
        FinancialAlertEntity value = repository.findByUserIdAndReferenceKey(user.getId(), key)
                .orElseGet(() -> {
                    FinancialAlertEntity alert = new FinancialAlertEntity();
                    alert.setUser(user);
                    alert.setReferenceKey(key);
                    alert.setRead(false);
                    return alert;
                });
        value.setAlertType(type);
        value.setSeverity(severity);
        value.setTitle(title);
        value.setMessage(message);
        value = repository.save(value);
        sendPendingEmail(value);
    }

    private void sendPendingEmail(FinancialAlertEntity alert) {
        if (alert.getEmailSentAt() != null) return;
        UserEntity user = alert.getUser();
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombre", user.getFullName());
            variables.put("titulo", alert.getTitle());
            variables.put("mensaje", alert.getMessage());
            variables.put("severidad", severityLabel(alert.getSeverity()));
            variables.put("fecha", LocalDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            emailService.sendEmail(EmailRequest.builder()
                    .to(List.of(user.getUsername()))
                    .subject("Life Master: " + alert.getTitle())
                    .templateName("alerta_financiera")
                    .variables(variables)
                    .imagenes(Map.of("logoApp", ImageDto.builder()
                            .uniqueCID("logoApp")
                            .nameResource("logo.png")
                            .build()))
                    .build());
            alert.setEmailSentAt(LocalDateTime.now(ZoneId.systemDefault()));
            repository.save(alert);
        } catch (RuntimeException exception) {
            log.error("No fue posible enviar por correo la alerta {} del usuario {}. Se reintentará después.",
                    alert.getId(), user.getId(), exception);
        }
    }

    private String severityLabel(String severity) {
        return switch (severity) {
            case "CRITICAL" -> "Crítica";
            case "HIGH" -> "Alta";
            case "MEDIUM" -> "Media";
            default -> "Informativa";
        };
    }

    private String formatMoney(java.math.BigDecimal amount, String currency) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-MX"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "$" + format.format(amount) + " " + currency;
    }

    private FinancialAlertResponse response(FinancialAlertEntity value) {
        return new FinancialAlertResponse(value.getId(), value.getAlertType(), value.getSeverity(), value.getTitle(),
                value.getMessage(), value.getRead(), value.getCreatedAt());
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "Alert resource not found: " + id);
    }
}
