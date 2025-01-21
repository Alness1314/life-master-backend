package com.alness.lifemaster.expenses.controller;

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
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.dto.response.ExpensesResponse;
import com.alness.lifemaster.expenses.service.ExpensesService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/usuarios")
@Tag(name = "Expenses", description = ".")
public class ExpensesController {
     @Autowired
    private ExpensesService expensesService;

    @GetMapping("/{userId}/expenses")
    public ResponseEntity<List<ExpensesResponse>> findAll(@PathVariable String userId, @RequestParam Map<String, String> parameters) {
        List<ExpensesResponse> response = expensesService.find( userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/expenses/{id}")
    public ResponseEntity<ExpensesResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        ExpensesResponse response = expensesService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/expenses")
    public ResponseEntity<ExpensesResponse> save(@PathVariable String userId, @Valid @RequestBody ExpensesRequest request) {
        ExpensesResponse response = expensesService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/expenses/{id}")
    public ResponseEntity<ExpensesResponse> update(@PathVariable String userId, @PathVariable String id, @RequestBody ExpensesRequest request) {
        ExpensesResponse response = expensesService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/expenses/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseDto response = expensesService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
