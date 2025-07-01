package com.example.authservice.service;

import com.example.authservice.dto.UserDto;
import com.example.authservice.dto.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String url = "http://user-service/internal/users/username/";

    public String loginAndGenerateToken(AuthRequest authRequest) {
        // 1. Gọi User Service để lấy thông tin user
        UserDto user;
        try {
            // "user-service" là tên đã đăng ký với Eureka
            String url = "http://user-service/internal/users/username/" + authRequest.getUsername();
            user = restTemplate.getForObject(url, UserDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("User not found or Invalid credentials");
        }

        if (user == null) {
            throw new RuntimeException("Invalid credentials");
        }

        // 2. So sánh mật khẩu
        if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            // 3. Nếu khớp, tạo JWT token
            return jwtService.generateToken(user.getUsername());
        } else {
            // Nếu không khớp, ném ra ngoại lệ
            throw new RuntimeException("Invalid credentials");
        }
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
    public void register(AuthRequest authRequest) {
        jwtService.validateToken(token);
    }

}
