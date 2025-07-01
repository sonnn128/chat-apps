package com.example.authservice.controller;

import com.example.authservice.dto.IntrospectRequest;
import com.example.authservice.dto.IntrospectResponse;
import com.example.authservice.dto.request.AuthRequest;
import com.example.authservice.dto.response.AuthResponse;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        String token = authService.loginAndGenerateToken(authRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest authRequest) {
        String token = authService.register(authRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        String token = request.getToken();
        boolean isValid = tokenProvider.validateToken(token);

        Claims claims = tokenProvider.getClaimsFromToken(token);
        String username = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return ResponseEntity.ok(new IntrospectResponse(true, username, roles));
    }


}
