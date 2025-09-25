package com.sonnguyen.notificationservice.events;

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
public class FriendRequestSentEvent {
    public static final String EVENT_TYPE = "FRIEND_REQUEST_SENT";

    private UUID requesterId;
    private UUID friendId;
    private LocalDateTime createdAt;
}
