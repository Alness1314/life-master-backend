package com.alness.lifemaster.users.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.users.dto.request.UserSelfUpdateRequest;
import com.alness.lifemaster.users.dto.response.UserResponse;
import com.alness.lifemaster.users.service.UserService;
import com.alness.lifemaster.utils.ApiCodes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getOwnProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.findCurrentUser(authenticatedUserId(authentication)));
    }

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getOwnModules(
            Authentication authentication,
            @RequestParam(required = false) String level) {
        return ResponseEntity.ok(userService.findCurrentUserModules(authenticatedUserId(authentication), level));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateOwnProfile(
            Authentication authentication,
            @Valid @RequestBody UserSelfUpdateRequest request) {
        return ResponseEntity.accepted().body(userService.updateSelf(authenticatedUserId(authentication), request));
    }

    private UUID authenticatedUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof UUID userId)) {
            throw new RestExceptionHandler(
                    ApiCodes.API_CODE_401,
                    HttpStatus.UNAUTHORIZED,
                    "No fue posible identificar al usuario autenticado.");
        }
        return userId;
    }
}
