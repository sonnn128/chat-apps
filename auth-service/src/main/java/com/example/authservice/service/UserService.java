package com.example.authservice.service;

import com.example.authservice.client.UserClient;
import com.example.authservice.dto.request.UserRegistrationRequest;
import com.example.authservice.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserClient userClient;

    public UserResponse registerUser(UserRegistrationRequest request) {
        ResponseEntity<UserResponse> response = userClient.registerUser(request);
        return response.getBody();
    }

}
