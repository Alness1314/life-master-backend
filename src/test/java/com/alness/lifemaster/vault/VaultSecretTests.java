package com.alness.lifemaster.vault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.TextEncrypterUtil;
import com.alness.lifemaster.vault.entity.VaultEntity;
import com.alness.lifemaster.vault.repository.VaultRepository;
import com.alness.lifemaster.vault.service.impl.VaultServiceImpl;

class VaultSecretTests {

    @Test
    void revealsTheDecryptedPasswordForTheRequestedVaultEntry() {
        String plainPassword = "My secure password";
        SecretKey key = TextEncrypterUtil.generateKey();
        VaultEntity vault = new VaultEntity();
        vault.setPasswordEncrypted(TextEncrypterUtil.encrypt(plainPassword, key));
        vault.setKey(TextEncrypterUtil.keyToString(key));

        VaultRepository vaultRepository = mock(VaultRepository.class);
        when(vaultRepository.findOne(any(Specification.class))).thenReturn(Optional.of(vault));
        VaultServiceImpl service = new VaultServiceImpl(
                vaultRepository,
                mock(UserRepository.class),
                mock(GenericMapper.class));

        var response = service.revealPassword(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        assertThat(response.password()).isEqualTo(plainPassword);
    }
}
