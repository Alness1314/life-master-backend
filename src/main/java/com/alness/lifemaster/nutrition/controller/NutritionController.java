package com.alness.lifemaster.nutrition.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.nutrition.dto.request.NutritionRequest;
import com.alness.lifemaster.nutrition.dto.response.NutritionResponse;
import com.alness.lifemaster.nutrition.dto.response.NutritionPhotoContent;
import com.alness.lifemaster.nutrition.service.NutritionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "Nutrition", description = ".")
@RequiredArgsConstructor
public class NutritionController {
    private final NutritionService nutritionService;

    @GetMapping("/{userId}/nutrition")
    public ResponseEntity<List<NutritionResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<NutritionResponse> response = nutritionService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/nutrition/{id}")
    public ResponseEntity<NutritionResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        NutritionResponse response = nutritionService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/{userId}/nutrition", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NutritionResponse> save(@PathVariable String userId,
            @Valid @RequestBody NutritionRequest request) {
        NutritionResponse response = nutritionService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{userId}/nutrition", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NutritionResponse> saveWithPhoto(@PathVariable String userId,
            @Valid @RequestPart("request") NutritionRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return new ResponseEntity<>(nutritionService.save(userId, request, photo), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{userId}/nutrition/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NutritionResponse> update(@PathVariable String userId, @PathVariable String id,
            @Valid @RequestBody NutritionRequest request) {
        NutritionResponse response = nutritionService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/{userId}/nutrition/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NutritionResponse> updateWithPhoto(@PathVariable String userId,
            @PathVariable String id,
            @Valid @RequestPart("request") NutritionRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return new ResponseEntity<>(nutritionService.update(userId, id, request, photo), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/nutrition/{id}/photo")
    public ResponseEntity<FileSystemResource> photo(@PathVariable String userId, @PathVariable String id) {
        NutritionPhotoContent photo = nutritionService.photo(userId, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(photo.originalName()).build().toString())
                .body(new FileSystemResource(photo.path()));
    }

    @DeleteMapping("/{userId}/nutrition/{id}/photo")
    public ResponseEntity<ResponseServerDto> deletePhoto(@PathVariable String userId, @PathVariable String id) {
        return new ResponseEntity<>(nutritionService.deletePhoto(userId, id), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/nutrition/{id}")
    public ResponseEntity<ResponseServerDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseServerDto response = nutritionService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
