package com.sonnguyen.notificationservice.events;

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
    private UUID userId;
    private String content;
    private ChannelMessageType type;
    private Instant timestamp;

    private List<UUID> recipientIds;
}
