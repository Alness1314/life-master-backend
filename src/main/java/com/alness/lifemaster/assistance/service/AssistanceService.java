package com.alness.lifemaster.assistance.service;

import java.util.List;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.common.dto.ResponseDto;

public interface AssistanceService {
    public AssistanceResponse findOne(String id);
    public List<AssistanceResponse> find();
    public AssistanceResponse save(AssistanceRequest request);
    public AssistanceResponse update(String id, AssistanceRequest request);
    public ResponseDto delete(String id);
}
