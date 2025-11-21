package com.nnson128.chatapps_base.models.events.friendship.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.friendship.FriendshipEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload for FRIEND_REQUEST_SENT event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestSentPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private FriendshipEventType eventType;

    @JsonProperty("sender_id")
    private UUID senderId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("recipient_id")
    private UUID recipientId;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("sent_at")
    private LocalDateTime sentAt;
}
