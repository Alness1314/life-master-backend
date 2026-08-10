package com.alness.lifemaster.nutrition.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionResponse;
import com.alness.lifemaster.nutrition.dto.response.NutritionPhotoContent;

public interface NutritionService {
    public NutritionResponse save(String userId, NutritionRequest request);
    public NutritionResponse save(String userId, NutritionRequest request, MultipartFile photo);
    public List<NutritionResponse> find(String userId, Map<String, String> params);
    public NutritionResponse findOne(String userId, String id);
    public NutritionResponse update(String userId, String id, NutritionRequest request);
    public NutritionResponse update(String userId, String id, NutritionRequest request, MultipartFile photo);
    public NutritionPhotoContent photo(String userId, String id);
    public ResponseServerDto deletePhoto(String userId, String id);
    public ResponseServerDto delete(String userId, String id);
}
