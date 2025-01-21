package com.alness.lifemaster.income.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.dto.response.IncomeResponse;

public interface IncomeService {
    public List<IncomeResponse> find(String userId, Map<String, String> params);
    public IncomeResponse findOne(String userId, String id);
    public IncomeResponse save(String userId, IncomeRequest request);
    public IncomeResponse update(String userId, String id, IncomeRequest request);
    public ResponseDto delete(String userId, String id);
}
