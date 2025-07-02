package com.example.authservice.controller;

import com.example.authservice.dto.IntrospectRequest;
import com.example.authservice.dto.IntrospectResponse;
import com.example.authservice.dto.request.AuthRequest;
import com.example.authservice.dto.request.UserRegistrationRequest;
import com.example.authservice.dto.response.AuthResponse;
import com.example.authservice.service.AuthService;
import com.example.authservice.service.JwtService;
import com.example.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        return ResponseEntity.ok(userService.registerUser(userRegistrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        IntrospectResponse response = jwtService.introspectToken(request.getToken());
        log.info("IntrospectResponse introspection response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("OK");
    }
}
