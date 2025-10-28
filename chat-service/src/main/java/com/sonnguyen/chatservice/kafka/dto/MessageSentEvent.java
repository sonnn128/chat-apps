package com.sonnguyen.chatservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSentEvent {
    public static final String EVENT_TYPE = "MESSAGE_SENT";

    private MessageSentEventKey key;
    private String type;
    private UUID userId;
    private String content;
    private Instant timestamp;
    private String senderName;
    private String senderAvatar;
    private List<UUID> recipientIds;
}
