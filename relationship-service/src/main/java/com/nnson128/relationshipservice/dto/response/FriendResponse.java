package com.nnson128.relationshipservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private UUID friendId;
    private String firstname;
    private String lastname;
    private String email;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private String status;
}
