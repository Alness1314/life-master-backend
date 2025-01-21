package com.alness.lifemaster.expenses.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.dto.response.ExpensesResponse;

public interface ExpensesService {
    public List<ExpensesResponse> find(String userId, Map<String, String> params);
    public ExpensesResponse findOne(String userId, String id);
    public ExpensesResponse save(String userId, ExpensesRequest request);
    public ExpensesResponse update(String userId, String id, ExpensesRequest request);
    public ResponseDto delete(String userId, String id);
}
