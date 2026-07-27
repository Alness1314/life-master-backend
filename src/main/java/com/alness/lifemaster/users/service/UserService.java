package com.alness.lifemaster.users.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.dto.ValueExistenceResponse;
import com.alness.lifemaster.users.dto.request.UserRequest;
import com.alness.lifemaster.users.dto.request.UserSelfUpdateRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;

public interface UserService {
    public UserResponse save(UserRequest request);
    public UserResponse findOne(String id);
    public UserResponse findByUsername(String username);
    public List<UserResponse> find(Map<String, String> params);
    public UserResponse update(String id, UserRequest request);
    public UserResponse updateSelf(UUID userId, UserSelfUpdateRequest request);
    public ValueExistenceResponse validateExistingValues(Map<String, String> values, UUID excludeId);
    public ResponseServerDto delete(String id);
}
