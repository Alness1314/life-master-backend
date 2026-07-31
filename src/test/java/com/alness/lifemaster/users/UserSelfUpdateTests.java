package com.alness.lifemaster.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.files.StoredFileRepository;
import com.alness.lifemaster.common.validation.GenericExistenceValidator;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.profiles.repository.ProfileRepository;
import com.alness.lifemaster.users.dto.request.UserSelfUpdateRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.users.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserSelfUpdateTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GenericMapper mapper;
    @Mock
    private StoredFileRepository storedFileRepository;

    private UserServiceImpl service;
    private UUID userId;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository,
                profileRepository,
                passwordEncoder,
                mapper,
                new GenericExistenceValidator(),
                storedFileRepository);
        userId = UUID.randomUUID();
        user = new UserEntity();
        user.setId(userId);
        user.setUsername("original@example.com");
        user.setFullName("Nombre original");
        user.setPassword("encoded-old-password");
        user.setProfiles(List.of(new ProfileEntity()));
        user.setVerified(true);
        user.setErased(false);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
    }

    @Test
    void updatesOnlyAllowedProfileFields() {
        UUID imageId = UUID.randomUUID();
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();
        request.setFullName("  Nombre actualizado  ");
        request.setPassword("new-password-123");
        request.setImageId(imageId.toString());
        when(passwordEncoder.encode("new-password-123")).thenReturn("encoded-new-password");
        when(storedFileRepository.existsByIdAndUserIdAndErasedFalse(imageId, userId)).thenReturn(true);
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(any(UserEntity.class), any())).thenReturn(new UserResponse());

        service.updateSelf(userId, request);

        assertThat(user.getFullName()).isEqualTo("Nombre actualizado");
        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        assertThat(user.getImageId()).isEqualTo(imageId);
        assertThat(user.getUsername()).isEqualTo("original@example.com");
        assertThat(user.getProfiles()).hasSize(1);
        assertThat(user.getVerified()).isTrue();
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void rejectsRequestsWithoutAllowedChanges() {
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();

        assertThatThrownBy(() -> service.updateSelf(userId, request))
                .isInstanceOf(RestExceptionHandler.class)
                .hasMessageContaining("al menos un campo");
        verify(userRepository, never()).saveAndFlush(any());
    }
}
