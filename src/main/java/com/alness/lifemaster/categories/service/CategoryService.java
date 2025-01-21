package com.alness.lifemaster.categories.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.categories.dto.request.CategoryRequest;
import com.alness.lifemaster.categories.dto.response.CategoryResponse;
import com.alness.lifemaster.common.dto.ResponseDto;

public interface CategoryService {
    public List<CategoryResponse> find(Map<String, String> params);
    public CategoryResponse findOne(String id);
    public CategoryResponse save(CategoryRequest request);
    public CategoryResponse update(String id, CategoryRequest request);
    public ResponseDto delete(String id);
}
