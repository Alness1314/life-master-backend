package com.alness.lifemaster.profiles.controller;

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

import com.alness.lifemaster.profiles.dto.request.ProfileRequest;
import com.alness.lifemaster.profiles.dto.response.ProfileResponse;
import com.alness.lifemaster.profiles.service.ProfileService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/profiles")
@Tag(name = "Profiles", description = ".")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> findAll(@RequestParam Map<String, String> param) {
        List<ProfileResponse> response = profileService.find(param);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> findOne(@PathVariable String id) {
        ProfileResponse response = profileService.findOne(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> postMethodName(@RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
