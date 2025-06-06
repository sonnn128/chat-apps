package com.sonnguyen.chatapi.service.impl;

import com.sonnguyen.chatapi.config.JwtService;
import com.sonnguyen.chatapi.exception.CommonException;
import com.sonnguyen.chatapi.model.Role;
import com.sonnguyen.chatapi.model.User;
import com.sonnguyen.chatapi.payload.request.LoginRequest;
import com.sonnguyen.chatapi.payload.request.RegisterRequest;
import com.sonnguyen.chatapi.payload.response.AuthResponse;
import com.sonnguyen.chatapi.repository.UserRepository;
import com.sonnguyen.chatapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final int JWT_EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CommonException("User not found", HttpStatus.NOT_FOUND));

        String token = jwtService.generateToken(user, JWT_EXPIRATION_TIME);

        return buildAuthResponse(user, token);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CommonException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user, JWT_EXPIRATION_TIME);

        return buildAuthResponse(user, jwtToken);
    }

    @Override
    public AuthResponse authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User user)) {
            throw new CommonException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String jwt = jwtService.generateToken(user, JWT_EXPIRATION_TIME);

        return buildAuthResponse(user, jwt);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
