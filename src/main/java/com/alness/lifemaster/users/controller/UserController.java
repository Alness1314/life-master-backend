package com.alness.lifemaster.users.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.users.dto.request.UserRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/users")
@Tag(name = "Users", description = ".")
public class UserController {
    @Autowired
    private UserService userService;

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
    public ResponseEntity<UserResponse> postMethodName(@RequestBody UserRequest request) {
        UserResponse response = userService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> postMethodName(@PathVariable String id) {
        ResponseDto response = userService.delete(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
