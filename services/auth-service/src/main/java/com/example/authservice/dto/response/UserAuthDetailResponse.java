package com.example.authservice.dto.response;

import com.example.authservice.model.Role;
import lombok.Data;
import java.util.UUID;

@Data
public class UserAuthDetailResponse {
    private UUID id;
    private String email;
    private String password;
    private Role role;
}
