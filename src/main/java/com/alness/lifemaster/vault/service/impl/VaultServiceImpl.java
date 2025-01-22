package com.alness.lifemaster.vault.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.TextEncrypterUtil;
import com.alness.lifemaster.vault.dto.request.VaultRequest;
import com.alness.lifemaster.vault.dto.response.VaultResponse;
import com.alness.lifemaster.vault.entity.VaultEntity;
import com.alness.lifemaster.vault.repository.VaultRepository;
import com.alness.lifemaster.vault.service.VaultService;
import com.alness.lifemaster.vault.specification.VaultSpecification;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VaultServiceImpl implements VaultService {

    @Autowired
    private VaultRepository vaultRepository;

    @Autowired
    private UserRepository userRepository;

    private ModelMapper mapper = new ModelMapper();

    @PostConstruct
    public void init() {
        configureModelMapper();
    }

    private void configureModelMapper() {
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public List<VaultResponse> find(String userId, Map<String, String> params) {
        params.put("user", userId);
        Specification<VaultEntity> specification = filterWithParameters(params);
        return vaultRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .collect(Collectors.toList());
    }

    @Override
    public VaultResponse findOne(String userId, String id) {
        VaultEntity vault = vaultRepository.findOne(filterWithParameters(Map.of("id", id, "user", userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Income not found."));
        return mapperDto(vault);
    }

    @Override
    public VaultResponse save(String userId, VaultRequest request) {
        VaultEntity vault = mapper.map(request, VaultEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(
                        () -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "User not found."));
        vault.setUser(user);
        try {
            Map<String, String> params = secureIn(vault.getPasswordEncrypted());
            vault.setPasswordEncrypted(params.get("encode"));
            vault.setKey(params.get("key"));
            vault = vaultRepository.save(vault);
        } catch (Exception e) {
            log.error("Error to save income {}", e.getMessage());
            e.printStackTrace();
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to save invome.");
        }
        return mapperDto(vault);
    }

    private Map<String, String> secureIn(String password){
        SecretKey key = TextEncrypterUtil.generateKey();
        String passwordEncode = TextEncrypterUtil.encrypt(password, key);
        String ivKey = TextEncrypterUtil.keyToString(key);

        return Map.of("encode", passwordEncode, "key", ivKey);
    }

    @Override
    public VaultResponse update(String userId, String id, VaultRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private VaultResponse mapperDto(VaultEntity source) {
        return mapper.map(source, VaultResponse.class);
    }

    public Specification<VaultEntity> filterWithParameters(Map<String, String> parameters) {
        return new VaultSpecification().getSpecificationByFilters(parameters);
    }

}
