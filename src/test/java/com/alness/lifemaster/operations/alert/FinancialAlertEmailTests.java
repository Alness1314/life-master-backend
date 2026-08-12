package com.alness.lifemaster.operations.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.finance.recurring.RecurringMovementRepository;
import com.alness.lifemaster.sender.dto.EmailRequest;
import com.alness.lifemaster.sender.service.EmailService;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FinancialAlertEmailTests {
    @Mock private FinancialAlertRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private DebtsRespository debtsRepository;
    @Mock private RecurringMovementRepository recurringRepository;
    @Mock private EmailService emailService;
    @InjectMocks private FinancialAlertService service;

    @Test
    void sendsTemplateEmailOnlyOnceForTheSameAlert() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("usuario@example.com");
        user.setFullName("Usuario de prueba");

        DebtsEntity debt = new DebtsEntity();
        debt.setId(UUID.randomUUID());
        debt.setCreditorName("Banco");
        debt.setTotalAmount(new BigDecimal("1250.50"));
        debt.setCurrency("MXN");
        debt.setDueDate(LocalDate.now().plusDays(2));
        debt.setPayments(List.of());

        AtomicReference<FinancialAlertEntity> stored = new AtomicReference<>();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(debtsRepository.findAllByUserIdAndErasedFalse(userId)).thenReturn(List.of(debt));
        when(recurringRepository.findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(any(), any()))
                .thenReturn(List.of());
        when(repository.findByUserIdAndReferenceKey(any(), any()))
                .thenAnswer(invocation -> Optional.ofNullable(stored.get()));
        when(repository.save(any(FinancialAlertEntity.class))).thenAnswer(invocation -> {
            FinancialAlertEntity alert = invocation.getArgument(0);
            if (alert.getId() == null) alert.setId(UUID.randomUUID());
            stored.set(alert);
            return alert;
        });
        when(repository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenAnswer(invocation -> stored.get() == null ? List.of() : List.of(stored.get()));

        service.refresh(userId);
        service.refresh(userId);

        ArgumentCaptor<EmailRequest> request = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService, times(1)).sendEmail(request.capture());
        assertThat(request.getValue().getTo()).containsExactly("usuario@example.com");
        assertThat(request.getValue().getTemplateName()).isEqualTo("alerta_financiera");
        assertThat(request.getValue().getVariables()).containsEntry("nombre", "Usuario de prueba");
        assertThat(request.getValue().getImagenes().get("logoApp").getNameResource()).isEqualTo("logo.png");
        assertThat(request.getValue().getImagenes().get("logoApp").getUniqueCID()).isEqualTo("logoApp");
        assertThat(stored.get().getEmailSentAt()).isNotNull();
    }
}
