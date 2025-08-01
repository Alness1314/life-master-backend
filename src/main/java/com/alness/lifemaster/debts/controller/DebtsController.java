package com.alness.lifemaster.debts.controller;

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
import com.alness.lifemaster.debts.dto.request.DebtsRequest;
import com.alness.lifemaster.debts.dto.response.DebtsResponse;
import com.alness.lifemaster.debts.service.DebtsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "Debts", description = ".")
@RequiredArgsConstructor
public class DebtsController {
    private final DebtsService debtsService;

    @GetMapping("/{userId}/debts")
    public ResponseEntity<List<DebtsResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<DebtsResponse> response = debtsService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/debts/{id}")
    public ResponseEntity<DebtsResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        DebtsResponse response = debtsService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/debts")
    public ResponseEntity<DebtsResponse> save(@PathVariable String userId,
            @Valid @RequestBody DebtsRequest request) {
        DebtsResponse response = debtsService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/debts/{id}")
    public ResponseEntity<DebtsResponse> update(@PathVariable String userId, @PathVariable String id,
            @RequestBody DebtsRequest request) {
        DebtsResponse response = debtsService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/debts/{id}")
    public ResponseEntity<ResponseServerDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseServerDto response = debtsService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
