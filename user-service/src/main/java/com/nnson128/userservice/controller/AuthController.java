package com.nnson128.userservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.userservice.dto.request.ForgotPasswordRequest;
import com.nnson128.userservice.dto.request.LoginRequest;
import com.nnson128.userservice.dto.request.UserRegistrationRequest;
import com.nnson128.userservice.dto.response.AuthResponse;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().body(ApiResponse.builder()
            .message("Successfully logged in")
            .success(true)
            .data(authService.login(request))
            .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.builder()
            .message("Successfully logged in")
            .success(true)
            .data(response)
            .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/introspect")
    public ResponseEntity<?> introspectToken(@RequestBody String token) {
        return ResponseEntity.ok(authService.introspectToken(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Password reset email sent")
            .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody com.nnson128.userservice.dto.request.ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Password successfully reset")
            .build());
    }
}
