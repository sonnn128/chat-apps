package com.sonnguyen.chatservice.events.dto;

import com.sonnguyen.chatservice.model.ChannelMessageType;
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
public class NewMessageSentEvent {
    private final String eventType = "NEW_MESSAGE";

    private NewMessageSentEventKey key;
    private UUID userId;
    private String content;
    private ChannelMessageType type;
    private Instant timestamp;

    private List<UUID> recipientIds;
}

