package com.nnson128.userservice.service;

import com.nnson128.chatapps_base.exception.CommonException;
import com.nnson128.userservice.dto.identity.Credential;
import com.nnson128.userservice.dto.identity.TokenExchangeParam;
import com.nnson128.userservice.dto.identity.UserCreationParam;
import com.nnson128.userservice.dto.request.LoginRequest;
import com.nnson128.userservice.dto.request.UserRegistrationRequest;
import com.nnson128.userservice.dto.response.AuthResponse;
import com.nnson128.userservice.dto.response.IntrospectResponse;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.model.User;
import com.nnson128.userservice.repository.IdentityClient;
import com.nnson128.userservice.repository.UserRepository;
import com.nnson128.userservice.util.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final IdentityClient identityClient;

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CommonException("Invalid credentials", HttpStatus.UNAUTHORIZED));


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CommonException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        return AuthResponse.builder()
                .accessToken(getTokenFromKeycloak(request.getEmail(), request.getPassword()))
                .user(UserResponse.fromUser(user))
                .build();

    }

    public UserResponse register(UserRegistrationRequest request) {
        // 1. Normalize phone number
        String normalizedPhone = PhoneNumberUtils.normalizeVietnamesePhone(request.getPhone());
        
        // 2. Validate phone number
        if (!PhoneNumberUtils.isValidVietnamesePhone(normalizedPhone)) {
            throw new CommonException("Invalid phone number format: " + request.getPhone(), HttpStatus.BAD_REQUEST);
        }
        
        // 3. Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new CommonException("Email " + request.getEmail() + " is already in use.", HttpStatus.CONFLICT);
        });

        userRepository.findByPhone(normalizedPhone).ifPresent(user -> {
            throw new CommonException("Phone " + normalizedPhone + " is already in use.", HttpStatus.CONFLICT);
        });

        String token = identityClient.getClientToken(TokenExchangeParam.builder()
                .grant_type("client_credentials")
                .client_id(clientId)
                .client_secret(clientSecret)
                .scope("openid")
                .build()).getAccessToken();

        //        call api create user in admin api
        var creationResponse = identityClient.createUser(
                "Bearer " + token,
                UserCreationParam.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstname())
                        .lastName(request.getLastname())
                        .email(request.getEmail())
                        .enabled(true)
                        .emailVerified(false)
                        .credentials(List.of(Credential.builder()
                                .type("password")
                                .temporary(false)
                                .value(request.getPassword())
                                .build()))
                        .build());

//        get userid from keycloak response
        UUID userId = extractUserId(creationResponse);

        // 4. Create user in local database
        User newUser = User.builder()
                .id(userId)
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(normalizedPhone) // Use normalized phone number
                .build();

        User savedUser = userRepository.save(newUser);

        return UserResponse.fromUser(savedUser);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String url = keycloakAuthUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                return AuthResponse.builder()
                        .accessToken((String) tokenResponse.get("access_token"))
                        .refreshToken((String) tokenResponse.get("refresh_token"))
                        .tokenType("Bearer")
                        .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                        .build();
            }
        } catch (Exception e) {
        }

        throw new CommonException("Failed to refresh token", HttpStatus.UNAUTHORIZED);
    }

    public void logout(String token) {
        String accessToken = token.replace("Bearer ", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String url = keycloakAuthUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
        }
    }

    public IntrospectResponse introspectToken(String token) {
        String accessToken = token.replace("Bearer ", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", accessToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String url = keycloakAuthUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> introspectData = response.getBody();
                Boolean active = (Boolean) introspectData.get("active");

                if (active != null && active) {
                    return IntrospectResponse.builder()
                            .active(true)
                            .username((String) introspectData.get("preferred_username"))
                            .userId(UUID.fromString((String) introspectData.get("sub")))
                            .roles(Arrays.asList(((String) introspectData.get("realm_access")).split(",")))
                            .exp(((Number) introspectData.get("exp")).longValue())
                            .iat(((Number) introspectData.get("iat")).longValue())
                            .sub((String) introspectData.get("sub"))
                            .build();
                }
            }
        } catch (Exception e) {
        }

        return IntrospectResponse.inactive();
    }

    private String getTokenFromKeycloak(String email, String password) {

        return identityClient.getUserToken(TokenExchangeParam.builder()
                .grant_type("password")
                .client_id(clientId)
                .username(email)
                .password(password)
                .client_secret(clientSecret)
                .scope("openid")
                .build()).getAccessToken();
    }

    private void createUserInKeycloak(UserRegistrationRequest request) {
//        exchange client token

        // For now, we'll just log it
    }

    private UUID extractUserId(ResponseEntity<?> response) {
        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IllegalStateException("Location header is missing in response");
        }
        String path = location.getPath();
        String idStr = path.substring(path.lastIndexOf("/") + 1);
        return UUID.fromString(idStr); // Convert string sang UUID
    }

}
