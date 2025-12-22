package com.nnson128.chatapps_base.models.events.channel.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.channel.ChannelEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Payload for MEMBERS_REMOVED_FROM_CHANNEL event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRemovedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private ChannelEventType eventType;

    @JsonProperty("channel_id")
    private UUID channelId;

    @JsonProperty("removed_by_user_id")
    private UUID removedByUserId;

    @JsonProperty("removed_member_ids")
    private List<UUID> removedMemberIds;

    @JsonProperty("remaining_member_ids")
    private List<UUID> remainingMemberIds;
}
