package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.recurring-movements.scheduler-enabled",
        havingValue = "true", matchIfMissing = true)
public class RecurringMovementScheduler {
    private final RecurringMovementRepository repository;
    private final RecurringMovementService service;

    @Value("${app.recurring-movements.zone:America/Mexico_City}")
    private String zone;

    @Scheduled(cron = "${app.recurring-movements.cron:0 5 0 * * *}",
            zone = "${app.recurring-movements.zone:America/Mexico_City}")
    public void generateDueMovements() {
        LocalDate through = LocalDate.now(ZoneId.of(zone));
        int processed = 0;
        int failed = 0;

        for (UUID recurringId : repository.findDueIds(through)) {
            try {
                service.generateOneDue(recurringId, through);
                processed++;
            } catch (RuntimeException exception) {
                failed++;
                log.error("No se pudo generar el movimiento recurrente {} para la fecha {}.",
                        recurringId, through, exception);
            }
        }

        if (processed > 0 || failed > 0) {
            log.info("Generación automática finalizada: fecha={}, procesados={}, fallidos={}.",
                    through, processed, failed);
        }
    }
}
