package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IntrospectResponse(boolean valid, String username, List<String> roles) {
    public IntrospectResponse(boolean valid) {
        this(valid, null, null);
    }

}