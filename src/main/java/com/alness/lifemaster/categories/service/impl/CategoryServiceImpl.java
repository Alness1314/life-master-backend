package com.alness.lifemaster.categories.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.categories.dto.request.CategoryRequest;
import com.alness.lifemaster.categories.dto.response.CategoryResponse;
import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.categories.service.CategoryService;
import com.alness.lifemaster.categories.specification.CategorySpecification;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    private ModelMapper mapper = new ModelMapper();

    @Override
    public List<CategoryResponse> find(Map<String, String> params) {
        Specification<CategoryEntity> specification = filterWithParameters(params);
        return categoryRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse findOne(String id) {
        CategoryEntity category = categoryRepository.findOne(filterWithParameters(Map.of("id", id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Category not found"));
        return mapperDto(category);
    }

    @Override
    public CategoryResponse save(CategoryRequest request) {
        CategoryEntity category = mapper.map(request, CategoryEntity.class);
        try {
            category = categoryRepository.save(category);
        } catch (Exception e) {
            log.error("Error to save category {}", e.getMessage());
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to save category");
        }
        return mapperDto(category);
    }

    @Override
    public CategoryResponse update(String id, CategoryRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private CategoryResponse mapperDto(CategoryEntity source) {
        return mapper.map(source, CategoryResponse.class);
    }

    public Specification<CategoryEntity> filterWithParameters(Map<String, String> parameters) {
        return new CategorySpecification().getSpecificationByFilters(parameters);
    }
}
