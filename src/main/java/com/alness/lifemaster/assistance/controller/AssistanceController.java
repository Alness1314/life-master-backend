package com.alness.lifemaster.assistance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.assistance.service.AssistanceService;
import com.alness.lifemaster.common.dto.ResponseDto;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/usuarios")
@Tag(name = "Assistance", description = ".")
public class AssistanceController {
    @Autowired
    private AssistanceService assistanceService;

    @GetMapping("/{userId}/assistance")
    public ResponseEntity<List<AssistanceResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> param) {
        List<AssistanceResponse> response = assistanceService.find(userId, param);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}/assistance/{id}")
    public ResponseEntity<AssistanceResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        AssistanceResponse response = assistanceService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{userId}/assistance")
    public ResponseEntity<AssistanceResponse> postMethodName(@PathVariable String userId,
            @RequestBody AssistanceRequest request) {
        AssistanceResponse response = assistanceService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/assistance/{id}")
    public ResponseEntity<ResponseDto> postMethodName(@PathVariable String userId,
            @PathVariable String id) {
        ResponseDto response = assistanceService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{userId}/assistance/{id}")
    public ResponseEntity<AssistanceResponse> patchAssistance(@PathVariable String userId, @PathVariable String id,
            @RequestParam String departureTime) {
        AssistanceResponse response = assistanceService.assignOutput(userId, id, departureTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
