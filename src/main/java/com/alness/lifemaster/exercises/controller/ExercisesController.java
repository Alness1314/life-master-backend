package com.alness.lifemaster.exercises.controller;

import java.util.List;
import java.util.Map;

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

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.exercises.dto.ExercisesRequest;
import com.alness.lifemaster.exercises.dto.ExercisesResponse;
import com.alness.lifemaster.exercises.service.ExercisesService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "Exercises", description = ".")
@RequiredArgsConstructor
public class ExercisesController {
    private final ExercisesService exercisesService;

    @GetMapping("/{userId}/exercises")
    public ResponseEntity<List<ExercisesResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<ExercisesResponse> response = exercisesService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/exercises/{id}")
    public ResponseEntity<ExercisesResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        ExercisesResponse response = exercisesService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/exercises")
    public ResponseEntity<ExercisesResponse> save(@PathVariable String userId,
            @Valid @RequestBody ExercisesRequest request) {
        ExercisesResponse response = exercisesService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/exercises/{id}")
    public ResponseEntity<ExercisesResponse> update(@PathVariable String userId, @PathVariable String id,
            @RequestBody ExercisesRequest request) {
        ExercisesResponse response = exercisesService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/exercises/{id}")
    public ResponseEntity<ResponseServerDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseServerDto response = exercisesService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
