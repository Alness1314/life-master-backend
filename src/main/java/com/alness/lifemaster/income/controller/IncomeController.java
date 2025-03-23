package com.alness.lifemaster.income.controller;

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

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.dto.response.IncomeResponse;
import com.alness.lifemaster.income.service.IncomeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/usuarios")
@Tag(name = "Income", description = ".")
public class IncomeController {
    @Autowired
    private IncomeService incomeService;

    @GetMapping("/{userId}/income")
    public ResponseEntity<List<IncomeResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<IncomeResponse> response = incomeService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/income/{id}")
    public ResponseEntity<IncomeResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        IncomeResponse response = incomeService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/income")
    public ResponseEntity<IncomeResponse> save(@PathVariable String userId, @Valid @RequestBody IncomeRequest request) {
        IncomeResponse response = incomeService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/income/{id}")
    public ResponseEntity<IncomeResponse> update(@PathVariable String userId, @PathVariable String id,
            @RequestBody IncomeRequest request) {
        IncomeResponse response = incomeService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/income/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseDto response = incomeService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
