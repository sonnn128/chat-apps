package com.nnson128.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectResponse {
    private boolean active;
    private String username;
    private UUID userId;
    private List<String> roles;
    private Long exp;
    private Long iat;
    private String sub;
    
    public static IntrospectResponse inactive() {
        return IntrospectResponse.builder()
                .active(false)
                .build();
    }
}
