package com.alness.lifemaster.debts.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.debts.dto.request.DebtsRequest;
import com.alness.lifemaster.debts.dto.response.DebtsResponse;

public interface DebtsService {
    public DebtsResponse save(String userId, DebtsRequest request);
    public List<DebtsResponse> find(String userId, Map<String, String> params);
    public DebtsResponse findOne(String userId, String id);
    public DebtsResponse update(String userId, String id, DebtsRequest request);
    public ResponseServerDto delete(String userId, String id);
}
