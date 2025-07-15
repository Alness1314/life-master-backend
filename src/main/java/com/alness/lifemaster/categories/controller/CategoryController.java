package com.alness.lifemaster.categories.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.categories.dto.request.CategoryRequest;
import com.alness.lifemaster.categories.dto.response.CategoryResponse;
import com.alness.lifemaster.categories.service.CategoryService;
import com.alness.lifemaster.common.dto.ResponseServerDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/category")
@Tag(name = "Categories", description = ".")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> findAll(@RequestParam Map<String, String> parameters) {
        List<CategoryResponse> response = categoryService.find(parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findOne(@PathVariable String id) {
        CategoryResponse response = categoryService.findOne(id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping()
    public ResponseEntity<CategoryResponse> save(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.save(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable String id, @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseServerDto> delete(@PathVariable String id) {
        ResponseServerDto response = categoryService.delete(id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
