package com.nnson128.relationshipservice.dto.response;

import com.nnson128.relationshipservice.model.friendship.Friendship;
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
public class FriendRequestResponse {
    private UUID requesterId;
    private String requesterFirstname;
    private String requesterLastname;
    private String requesterEmail;
    private String requesterAvatar;
    private LocalDateTime createdAt;
    private String status;

    public static FriendRequestResponse from(Friendship friendship) {
        return FriendRequestResponse.builder()
                .requesterId(friendship.getFriendshipKey().getRequesterId())
                .requesterFirstname("Unknown") // Will be populated by service
                .requesterLastname("User")
                .requesterEmail("unknown@example.com")
                .requesterAvatar(null)
                .createdAt(friendship.getCreatedAt())
                .status(friendship.getStatus().toString())
                .build();
    }
}
