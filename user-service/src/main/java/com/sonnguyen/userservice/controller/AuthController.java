package com.sonnguyen.userservice.controller;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.request.LoginRequest;
import com.sonnguyen.userservice.dto.request.UserRegistrationRequest;
import com.sonnguyen.userservice.dto.response.AuthResponse;
import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
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
        log.info("Registration attempt for email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Successfully logged in")
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("Token refresh attempt");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout attempt");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/introspect")
    public ResponseEntity<?> introspectToken(@RequestBody String token) {
        log.debug("Token introspection request");
        return ResponseEntity.ok(authService.introspectToken(token));
    }
}
