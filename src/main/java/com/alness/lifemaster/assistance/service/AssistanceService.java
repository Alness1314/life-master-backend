package com.alness.lifemaster.assistance.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.common.dto.ResponseDto;

public interface AssistanceService {
    public AssistanceResponse findOne(String userId, String id);
    public List<AssistanceResponse> find(String userId, Map<String, String> params);
    public AssistanceResponse save(String userId, AssistanceRequest request);
    public AssistanceResponse update(String userId, String id, AssistanceRequest request);
    public AssistanceResponse assignOutput(String userId, String id, String departureTime);
    public ResponseDto delete(String userId, String id);
}
