package com.alness.lifemaster.operations.alert;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alness.lifemaster.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FinancialAlertSchedulerTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialAlertService financialAlertService;

    @InjectMocks
    private FinancialAlertScheduler scheduler;

    @Test
    void continuesWithRemainingUsersWhenOneRefreshFails() {
        UUID failingUser = UUID.randomUUID();
        UUID validUser = UUID.randomUUID();
        when(userRepository.findAllActiveIds()).thenReturn(List.of(failingUser, validUser));
        doThrow(new IllegalStateException("test failure")).when(financialAlertService).refresh(failingUser);

        scheduler.refreshDailyAlerts();

        verify(financialAlertService).refresh(failingUser);
        verify(financialAlertService).refresh(validUser);
    }
}
