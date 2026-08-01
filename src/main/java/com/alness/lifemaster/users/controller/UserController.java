package com.alness.lifemaster.users.controller;

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
import com.alness.lifemaster.common.dto.ValueExistenceResponse;
import com.alness.lifemaster.users.dto.request.UserRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "Users", description = ".")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(@RequestParam Map<String, String> param) {
        List<UserResponse> response = userService.find(param);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findOne(@PathVariable String id) {
        UserResponse response = userService.findOne(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponse> postMethodName(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/validacion-valores-existentes")
    public ResponseEntity<ValueExistenceResponse> validateExistingValues(
            @RequestParam Map<String, String> parameters) {
        String excludeIdValue = parameters.remove("excludeId");
        java.util.UUID excludeId = excludeIdValue == null || excludeIdValue.isBlank()
                ? null
                : java.util.UUID.fromString(excludeIdValue);
        return ResponseEntity.ok(userService.validateExistingValues(parameters, excludeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseServerDto> postMethodName(@PathVariable String id) {
        ResponseServerDto response = userService.delete(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
