package com.example.authservice.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntrospectResponse {
    private boolean active;
    private String username;
    private UUID userId;
    private List<String> roles;
}