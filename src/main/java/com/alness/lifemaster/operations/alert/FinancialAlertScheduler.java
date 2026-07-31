package com.alness.lifemaster.operations.alert;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "app.financial-alerts",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FinancialAlertScheduler {
    private final UserRepository userRepository;
    private final FinancialAlertService financialAlertService;

    @Scheduled(
            cron = "${app.financial-alerts.cron:0 20 0 * * *}",
            zone = "${app.financial-alerts.zone:America/Mexico_City}")
    public void refreshDailyAlerts() {
        int refreshed = 0;
        int failed = 0;

        for (UUID userId : userRepository.findAllActiveIds()) {
            try {
                financialAlertService.refresh(userId);
                refreshed++;
            } catch (RuntimeException exception) {
                failed++;
                log.error("No fue posible regenerar las alertas del usuario {}", userId, exception);
            }
        }

        log.info("Regeneración diaria de alertas finalizada. Usuarios procesados: {}, errores: {}",
                refreshed, failed);
    }
}
