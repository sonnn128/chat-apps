package com.sonnguyen.friendshipservice.dto.response;

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
    private String friendFirstname;
    private String friendLastname;
    private String friendEmail;
    private String friendAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private String status;
}
