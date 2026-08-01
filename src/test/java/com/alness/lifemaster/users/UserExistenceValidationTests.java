package com.alness.lifemaster.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.alness.lifemaster.common.validation.GenericExistenceValidator;
import com.alness.lifemaster.common.validation.ValueExistence;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.files.StoredFileRepository;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.permissions.service.EffectivePermissionService;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.users.service.impl.UserServiceImpl;

class UserExistenceValidationTests {

    private UserRepository userRepository;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new UserServiceImpl(
                userRepository,
                mock(ProfileRepository.class),
                mock(PasswordEncoder.class),
                mock(GenericMapper.class),
                new GenericExistenceValidator(),
                mock(StoredFileRepository.class),
                mock(EffectivePermissionService.class));
    }

    @Test
    void reportsAnActiveUsernameAsExisting() {
        String username = "active@example.com";
        when(userRepository.existsByUsernameAndErasedFalse(username)).thenReturn(true);

        var response = service.validateExistingValues(Map.of("username", username), null);

        assertThat(response.data().get("username")).isEqualTo(ValueExistence.EXISTS);
        verify(userRepository).existsByUsernameAndErasedFalse(username);
    }

    @Test
    void reportsADeletedOrMissingUsernameAsAvailable() {
        String username = "deleted@example.com";
        when(userRepository.existsByUsernameAndErasedFalse(username)).thenReturn(false);

        var response = service.validateExistingValues(Map.of("username", username), null);

        assertThat(response.data().get("username")).isEqualTo(ValueExistence.NOT_EXIST);
    }

    @Test
    void excludesTheCurrentUserDuringAnEdit() {
        String username = "current@example.com";
        UUID currentUserId = UUID.randomUUID();
        when(userRepository.existsByUsernameAndErasedFalseAndIdNot(username, currentUserId)).thenReturn(false);

        var response = service.validateExistingValues(Map.of("username", username), currentUserId);

        assertThat(response.data().get("username")).isEqualTo(ValueExistence.NOT_EXIST);
        verify(userRepository).existsByUsernameAndErasedFalseAndIdNot(username, currentUserId);
    }
}
