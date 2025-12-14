package com.nnson128.userservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.model.User;
import com.nnson128.userservice.service.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeService meService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok().body(ApiResponse.<UserResponse>builder()
            .message("User profile")
            .success(true)
            .data(meService.getUserProfile(UUID.fromString(userId)))
            .build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody User updateRequest) {
        String userId = jwt != null ? jwt.getSubject() : null;
        UserResponse updatedUser = meService.updateUserProfile(UUID.fromString(userId), updateRequest);
        return ResponseEntity.ok().body(ApiResponse.<UserResponse>builder()
            .message("User profile updated successfully")
            .success(true)
            .data(updatedUser)
            .build());
    }

    @PutMapping("/avatar")
    public ResponseEntity<ApiResponse<Void>> updateUserAvatar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam("avatar") MultipartFile avatar) {
        // This method should use AvatarController instead
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
            .success(false)
            .message("Please use /api/v1/users/{userId}/avatar endpoint for avatar upload")
            .build());
    }
}
