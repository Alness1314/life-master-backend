package com.alness.lifemaster.sender.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.sender.dto.EmailRequest;
import com.alness.lifemaster.sender.service.EmailService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/helper")
@Tag(name = "E-Mail", description = ".")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<ResponseDto> getMethodName(@Valid @RequestBody EmailRequest request) {
        ResponseDto response = emailService.sendEmail(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
