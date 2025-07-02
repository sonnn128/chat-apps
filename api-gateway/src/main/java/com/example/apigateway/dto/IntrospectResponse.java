package com.example.apigateway.dto;

import java.util.List;

public record IntrospectResponse(boolean active, String username, List<String> roles) {}