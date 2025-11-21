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
 * Payload for FRIEND_REQUEST_ACCEPTED event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestAcceptedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private FriendshipEventType eventType;

    @JsonProperty("friend_1_id")
    private UUID friend1Id;

    @JsonProperty("friend_1_name")
    private String friend1Name;

    @JsonProperty("friend_2_id")
    private UUID friend2Id;

    @JsonProperty("friend_2_name")
    private String friend2Name;

    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @JsonProperty("direct_message_channel_id")
    private UUID directMessageChannelId;
}
