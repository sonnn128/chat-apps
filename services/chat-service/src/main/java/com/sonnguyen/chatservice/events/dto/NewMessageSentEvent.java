package com.sonnguyen.chatservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageSentEvent {
    private final String eventType = "NEW_MESSAGE";

    private UUID messageId;
    private UUID channelId;
    private String content;
    private Date timestamp;

    private UUID senderId;
    private String senderName;
    private String senderAvatar;

    private List<UUID> recipientIds;
}

