package com.alness.lifemaster.vault.controller;

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
import com.alness.lifemaster.vault.dto.request.VaultRequest;
import com.alness.lifemaster.vault.dto.response.VaultResponse;
import com.alness.lifemaster.vault.service.VaultService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/usuarios")
@Tag(name = "vault", description = ".")
public class VaultController {
    @Autowired
    private VaultService vaultService;

    @GetMapping("/{userId}/vault")
    public ResponseEntity<List<VaultResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<VaultResponse> response = vaultService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/vault/{id}")
    public ResponseEntity<VaultResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        VaultResponse response = vaultService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/vault")
    public ResponseEntity<VaultResponse> save(@PathVariable String userId, @Valid @RequestBody VaultRequest request) {
        VaultResponse response = vaultService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/vault/{id}")
    public ResponseEntity<VaultResponse> update(@PathVariable String userId, @PathVariable String id,
            @RequestBody VaultRequest request) {
        VaultResponse response = vaultService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/vault/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseDto response = vaultService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
