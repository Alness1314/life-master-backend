package com.alness.lifemaster.profiles.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.profiles.dto.request.ProfileRequest;
import com.alness.lifemaster.profiles.dto.response.ProfileResponse;

public interface ProfileService {
    public ProfileResponse save(ProfileRequest request);
    public ProfileResponse findOne(String id);
    public ProfileResponse findByName(String name);
    public List<ProfileResponse> find(Map<String, String> params);
    public ProfileResponse update(String id, ProfileRequest request);
    public ResponseDto delete(String id);
}
