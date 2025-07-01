package com.example.apigateway.dto;

import java.util.List;


public record IntrospectResponse(boolean valid, String username, List<String> roles) {}
