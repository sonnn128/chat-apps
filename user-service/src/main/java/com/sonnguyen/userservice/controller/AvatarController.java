package com.sonnguyen.userservice.controller;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.AvatarResponse;
import com.sonnguyen.userservice.service.AvatarService;
import com.sonnguyen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
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
            log.error("Error uploading avatar for user {}: {}", userId, e.getMessage());
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
            log.error("Error deleting avatar for user {}: {}", userId, e.getMessage());
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
            log.error("Error getting avatar for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to get avatar: " + e.getMessage())
                            .build());
        }
    }
}
