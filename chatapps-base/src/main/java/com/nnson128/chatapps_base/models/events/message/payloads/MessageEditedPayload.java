package com.nnson128.chatapps_base.models.events.message.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.message.MessageEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payload for MESSAGE_EDITED event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEditedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private MessageEventType eventType;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("edited_at")
    private Instant editedAt;

    @JsonProperty("recipient_ids")
    private List<UUID> recipientIds;
}
