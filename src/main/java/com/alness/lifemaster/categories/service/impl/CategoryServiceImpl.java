package com.alness.lifemaster.categories.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.categories.dto.request.CategoryRequest;
import com.alness.lifemaster.categories.dto.response.CategoryResponse;
import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.categories.repository.CategoryRepository;
import com.alness.lifemaster.categories.service.CategoryService;
import com.alness.lifemaster.categories.specification.CategorySpecification;
import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.mapper.GenericMapper;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.LoggerUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final GenericMapper mapper;

    @Override
    public List<CategoryResponse> find(Map<String, String> params) {
        Specification<CategoryEntity> specification = filterWithParameters(params);
        return categoryRepository.findAll(specification)
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public CategoryResponse findOne(String id) {
        CategoryEntity category = categoryRepository.findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(category);
    }

    @Override
    public CategoryResponse save(CategoryRequest request) {
        CategoryEntity category = mapper.map(request, CategoryEntity.class);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
        return mapperDto(category);
    }

    @Override
    public CategoryResponse update(String id, CategoryRequest request) {
        try {
            CategoryEntity existingCategory = categoryRepository
                    .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                    .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                            String.format(Messages.NOT_FOUND, id)));

            // Mapea los campos del request al objeto existente (sin sobrescribir el ID)
            mapper.map(request, existingCategory);

            CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
            return mapperDto(updatedCategory);

        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (RestExceptionHandler e) {
            throw e; // Relanza la excepción personalizada si ya fue lanzada arriba
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
    }

    @Override
    public ResponseServerDto delete(String id) {
        CategoryEntity existingCategory = categoryRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        try {
            categoryRepository.delete(existingCategory);
            return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    String.format(Messages.ERROR_ENTITY_DELETE, e.getMessage()));
        }
    }

    private CategoryResponse mapperDto(CategoryEntity source) {
        return mapper.map(source, CategoryResponse.class);
    }

    public Specification<CategoryEntity> filterWithParameters(Map<String, String> parameters) {
        return new CategorySpecification().getSpecificationByFilters(parameters);
    }
}
