package com.alness.lifemaster.assistance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.assistance.service.AssistanceService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/assistance")
@Tag(name = "Assistance", description = ".")
public class AssistanceController {
    @Autowired
    private AssistanceService assistanceService;


    @GetMapping
    public ResponseEntity<List<AssistanceResponse>> findAll(@RequestParam Map<String, String> param) {
        List<AssistanceResponse> response = assistanceService.find();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssistanceResponse> findOne(@PathVariable String id) {
        AssistanceResponse response = assistanceService.findOne(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AssistanceResponse> postMethodName(@RequestBody AssistanceRequest request) {
        AssistanceResponse response = assistanceService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
