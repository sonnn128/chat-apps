package com.sonnguyen.userservice.events.dto;

import com.sonnguyen.userservice.model.NewMessageSentEventKey;
import com.sonnguyen.userservice.model.message.ChannelMessageType;
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

    private NewMessageSentEventKey key;
    private UUID userId;
    private String content;
    private ChannelMessageType type;
    private Date timestamp;

    private List<UUID> recipientIds;
}
