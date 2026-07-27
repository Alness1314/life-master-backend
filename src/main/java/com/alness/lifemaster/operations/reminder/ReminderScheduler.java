package com.alness.lifemaster.operations.reminder;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.reminders.enabled", havingValue = "true")
public class ReminderScheduler {
    private final FinancialReminderRepository repository;
    private final JavaMailSender mailSender;

    @Scheduled(fixedDelayString = "${app.reminders.poll-ms:60000}")
    @Transactional
    public void deliverDue() {
        for (FinancialReminderEntity value : repository
                .findAllByDeliveredFalseAndCancelledFalseAndScheduledAtLessThanEqual(LocalDateTime.now())) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(value.getUser().getUsername());
            message.setSubject(value.getTitle());
            message.setText(value.getMessage());
            mailSender.send(message);
            value.setDelivered(true);
            repository.save(value);
        }
    }
}
