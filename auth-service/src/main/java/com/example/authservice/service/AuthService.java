package com.example.authservice.service;

import com.example.authservice.client.UserClient;
import com.example.authservice.dto.UserDto;
import com.example.authservice.dto.request.AuthRequest;
import com.example.authservice.dto.response.AuthResponse;
import com.example.authservice.dto.response.UserAuthDetailResponse;
import com.example.authservice.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;
    private final int JWT_EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    public AuthResponse login(AuthRequest request) {
        UserAuthDetailResponse userDetails;
        ResponseEntity<UserAuthDetailResponse> response = userClient.getUserByEmailForAuth(request.getEmail());
        userDetails = response.getBody();

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(userDetails, JWT_EXPIRATION_TIME);

        return new AuthResponse(token);
    }
}
