package com.sonnguyen.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarResponse {
    private String avatarUrl;
    private String message;
    
    public static AvatarResponse success(String avatarUrl) {
        return new AvatarResponse(avatarUrl, "Avatar updated successfully");
    }
    
    public static AvatarResponse deleted() {
        return new AvatarResponse(null, "Avatar deleted successfully");
    }
}
