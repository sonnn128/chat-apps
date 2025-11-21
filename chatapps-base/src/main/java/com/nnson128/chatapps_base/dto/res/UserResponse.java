package com.nnson128.chatapps_base.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String avatarUrl;
    private String phone;
    private String avatarPublicId;
}
