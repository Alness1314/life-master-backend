package com.alness.lifemaster.nutrition.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionResponse;

public interface NutritionService {
    public NutritionResponse save(String userId, NutritionRequest request);
    public List<NutritionResponse> find(String userId, Map<String, String> params);
    public NutritionResponse findOne(String userId, String id);
    public NutritionResponse update(String userId, String id, NutritionRequest request);
    public ResponseServerDto delete(String userId, String id);
}
