package com.nnson128.userservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.model.User;
import com.nnson128.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmailForAuth(@RequestParam("email") String email) {
        User user = userService.findUserByEmailForAuth(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<UserResponse>> searchUserByPhone(@RequestParam("phone") String phone) {
        UserResponse user = userService.searchUserByPhone(phone);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User found successfully")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok().body(ApiResponse.<UserResponse>builder()
                .message("User profile")
                .success(true)
                .data(userService.getUserProfile(userId))
                .build());
    }

    // Internal API for microservice communication (no ApiResponse wrapper)
    @GetMapping("/internal/{userId}")
    public ResponseEntity<UserResponse> getUserByIdInternal(@PathVariable UUID userId) {
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User has been deleted: " + id)
                .build());
    }

}