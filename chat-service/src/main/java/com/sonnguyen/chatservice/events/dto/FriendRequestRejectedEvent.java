package com.sonnguyen.chatservice.events.dto;

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
public class FriendRequestRejectedEvent {
    public static final String EVENT_TYPE = "FRIEND_REQUEST_REJECTED";

    private UUID requesterId;
    private UUID rejecterId;
    private LocalDateTime rejectedAt;
}
