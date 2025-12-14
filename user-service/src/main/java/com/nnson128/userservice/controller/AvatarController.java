package com.nnson128.userservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.userservice.dto.AvatarResponse;
import com.nnson128.userservice.service.AvatarService;
import com.nnson128.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AvatarController {

    private final UserService userService;
    private final AvatarService avatarService;

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<AvatarResponse>> uploadAvatar(
        @PathVariable UUID userId,
        @RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<AvatarResponse>builder()
                        .success(false)
                        .message("File is empty")
                        .build());
            }

            // Upload avatar using service
            String avatarUrl = avatarService.uploadAvatar(userId, file);

            AvatarResponse avatarResponse = AvatarResponse.success(avatarUrl);
            return ResponseEntity.ok(ApiResponse.<AvatarResponse>builder()
                .success(true)
                .message("Avatar uploaded successfully")
                .data(avatarResponse)
                .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<AvatarResponse>builder()
                    .success(false)
                    .message("Failed to upload avatar: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<AvatarResponse>> deleteAvatar(@PathVariable UUID userId) {
        try {
            // Delete avatar using service
            avatarService.deleteAvatar(userId);

            AvatarResponse avatarResponse = AvatarResponse.deleted();
            return ResponseEntity.ok(ApiResponse.<AvatarResponse>builder()
                .success(true)
                .message("Avatar deleted successfully")
                .data(avatarResponse)
                .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<AvatarResponse>builder()
                    .success(false)
                    .message("Failed to delete avatar: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<String>> getAvatar(@PathVariable UUID userId) {
        try {
            String avatarUrl = userService.getUserAvatarUrl(userId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Avatar retrieved successfully")
                .data(avatarUrl)
                .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Failed to get avatar: " + e.getMessage())
                    .build());
        }
    }
}
