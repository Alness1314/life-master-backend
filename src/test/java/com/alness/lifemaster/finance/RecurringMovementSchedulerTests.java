package com.alness.lifemaster.finance;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.alness.lifemaster.finance.recurring.RecurringMovementRepository;
import com.alness.lifemaster.finance.recurring.RecurringMovementScheduler;
import com.alness.lifemaster.finance.recurring.RecurringMovementService;

class RecurringMovementSchedulerTests {

    @Test
    void continuesProcessingWhenOneMovementFails() {
        RecurringMovementRepository repository = mock(RecurringMovementRepository.class);
        RecurringMovementService service = mock(RecurringMovementService.class);
        UUID failedId = UUID.randomUUID();
        UUID successfulId = UUID.randomUUID();
        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Mexico_City"));
        when(repository.findDueIds(today)).thenReturn(List.of(failedId, successfulId));
        doThrow(new IllegalStateException("test failure"))
                .when(service).generateOneDue(failedId, today);

        RecurringMovementScheduler scheduler = new RecurringMovementScheduler(repository, service);
        ReflectionTestUtils.setField(scheduler, "zone", "America/Mexico_City");
        scheduler.generateDueMovements();

        verify(service).generateOneDue(failedId, today);
        verify(service).generateOneDue(successfulId, today);
    }
}
