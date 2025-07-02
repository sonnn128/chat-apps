package com.example.apigateway.dto;

import java.util.List;
import java.util.UUID;

public record IntrospectResponse(boolean active, String username, UUID userId, List<String> roles) {}