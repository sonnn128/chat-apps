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
 * Payload for MESSAGE_SENT event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSentPayload implements Serializable {
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

    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_avatar")
    private String senderAvatar;

    @JsonProperty("recipient_ids")
    private List<UUID> recipientIds;
}
