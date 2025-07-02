package com.example.authservice.client;

import com.example.authservice.dto.request.AuthRequest;
import com.example.authservice.dto.request.UserRegistrationRequest;
import com.example.authservice.dto.response.AuthResponse;
import com.example.authservice.dto.response.UserAuthDetailResponse;
import com.example.authservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserClient {
    @PostMapping("/register")
    ResponseEntity<UserResponse> registerUser(@RequestBody UserRegistrationRequest request);

    @GetMapping("/by-email")
    ResponseEntity<UserAuthDetailResponse> getUserByEmailForAuth(@RequestParam("email") String email);
}
