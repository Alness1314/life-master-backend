package com.alness.lifemaster.vault.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.FuncUtils;
import com.alness.lifemaster.utils.LoggerUtil;
import com.alness.lifemaster.utils.TextEncrypterUtil;
import com.alness.lifemaster.vault.dto.request.VaultRequest;
import com.alness.lifemaster.vault.dto.response.VaultResponse;
import com.alness.lifemaster.vault.entity.VaultEntity;
import com.alness.lifemaster.vault.repository.VaultRepository;
import com.alness.lifemaster.vault.service.VaultService;
import com.alness.lifemaster.vault.specification.VaultSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final GenericMapper mapper;

    @Override
    public List<VaultResponse> find(String userId, Map<String, String> params) {
        Specification<VaultEntity> specification = filterWithParameters(FuncUtils.integrateUser(userId, params));
        return vaultRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public VaultResponse findOne(String userId, String id) {
        VaultEntity vault = vaultRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(vault);
    }

    @Override
    public VaultResponse save(String userId, VaultRequest request) {
        VaultEntity vault = mapper.map(request, VaultEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));
        vault.setUser(user);
        try {
            Map<String, String> params = secureIn(vault.getPasswordEncrypted());
            vault.setPasswordEncrypted(params.get(Filters.KEY_ENCODE));
            vault.setKey(params.get(Filters.KEY_PROP));
            vault = vaultRepository.save(vault);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
        return mapperDto(vault);
    }

    private Map<String, String> secureIn(String password) {
        SecretKey key = TextEncrypterUtil.generateKey();
        String passwordEncode = TextEncrypterUtil.encrypt(password, key);
        String ivKey = TextEncrypterUtil.keyToString(key);

        return Map.of(Filters.KEY_ENCODE, passwordEncode, Filters.KEY_PROP, ivKey);
    }

    @Override
    public VaultResponse update(String userId, String id, VaultRequest request) {
        VaultEntity existingVault = vaultRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        if (!existingVault.getUser().getId().toString().equals(userId)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_403, HttpStatus.FORBIDDEN, Messages.FORBIDEN_UPDATE_DATA);
        }

        try {
            // Actualiza campos desde el request
            existingVault.setSiteName(request.getSiteName());
            existingVault.setSiteUrl(request.getSiteUrl());
            existingVault.setUsername(request.getUsername());
            existingVault.setNotes(request.getNotes());

            // Encriptar de nuevo la contraseña (si se actualiza)
            Map<String, String> params = secureIn(request.getPasswordEncrypted());
            existingVault.setPasswordEncrypted(params.get(Filters.KEY_ENCODE));
            existingVault.setKey(params.get(Filters.KEY_PROP));

            existingVault = vaultRepository.save(existingVault);
            return mapperDto(existingVault);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        VaultEntity vault = vaultRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        try {
            vaultRepository.delete(vault);
            return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    String.format(Messages.ERROR_ENTITY_DELETE, e.getMessage()));
        }
    }

    private VaultResponse mapperDto(VaultEntity source) {
        return mapper.map(source, VaultResponse.class);
    }

    public Specification<VaultEntity> filterWithParameters(Map<String, String> parameters) {
        return new VaultSpecification().getSpecificationByFilters(parameters);
    }

}
