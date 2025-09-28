package com.sonnguyen.userservice.controller;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.request.UserRegistrationRequest;
import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.model.User;
import com.sonnguyen.userservice.repository.UserRepository;
import com.sonnguyen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmailForAuth(@RequestParam("email") String email) {
        User user = userService.findUserByEmailForAuth(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User user) {
        log.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<UserResponse>> searchUserByPhone(@RequestParam("phone") String phone) {
        log.info("Searching user by phone: {}", phone);
        try {
            UserResponse user = userService.searchUserByPhone(phone);
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User found successfully")
                    .data(user)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching user by phone {}: {}", phone, e.getMessage());
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("User not found with phone: " + phone)
                    .data(null)
                    .build();
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("Fetching user with id: {}", userId);
        return ResponseEntity.ok().body(ApiResponse.builder()
                        .message("User profile")
                        .success(true)
                        .data(userService.getUserProfile(UUID.fromString(userId)))
                .build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        log.info("Fetching user with id: {}", userId);
        return ResponseEntity.ok().body(ApiResponse.builder()
                        .message("User profile")
                        .success(true)
                        .data(userService.getUserProfile(userId))
                .build());
    }

    // Internal API for microservice communication (no ApiResponse wrapper)
    @GetMapping("/internal/{userId}")
    public ResponseEntity<UserResponse> getUserByIdInternal(@PathVariable UUID userId) {
        log.info("Internal API: Fetching user with id: {}", userId);
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody User updateRequest) {
        log.info("Updating user profile with id: {}", userId);
        UserResponse updatedUser = userService.updateUserProfile(UUID.fromString(userId), updateRequest);
        return ResponseEntity.ok().body(ApiResponse.builder()
                        .message("User profile updated successfully")
                        .success(true)
                        .data(updatedUser)
                .build());
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateUserAvatar(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("avatar") MultipartFile avatar) {
        log.info("Updating user avatar with id: {}", userId);
        // This method should use AvatarController instead
        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .success(false)
                .message("Please use /api/v1/users/{userId}/avatar endpoint for avatar upload")
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable UUID id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("User has been deleted: " + id)
                .build());
    }

}