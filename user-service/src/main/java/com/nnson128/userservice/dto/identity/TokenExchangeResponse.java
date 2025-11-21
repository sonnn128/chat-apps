package com.nnson128.userservice.dto.identity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenExchangeResponse {
    private String accessToken;
    private Long expiresIn; // Changed from String to Long
    private String refreshToken; // Keycloak usually returns 'refresh_token', not 'refresh_expires_in' for the token itself
    private String tokenType;
    private String idToken;
    private String scope;
}