package com.alness.lifemaster.vault.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.vault.dto.request.VaultRequest;
import com.alness.lifemaster.vault.dto.response.VaultResponse;

public interface VaultService {
    public List<VaultResponse> find(String userId, Map<String, String> params);
    public VaultResponse findOne(String userId, String id);
    public VaultResponse save(String userId, VaultRequest request);
    public VaultResponse update(String userId, String id, VaultRequest request);
    public ResponseServerDto delete(String userId, String id);
}
