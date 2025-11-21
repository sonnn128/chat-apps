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
 * Payload for MEMBERS_ADDED_TO_CHANNEL event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembersAddedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private ChannelEventType eventType;

    @JsonProperty("channel_id")
    private UUID channelId;

    @JsonProperty("channel_name")
    private String channelName;

    @JsonProperty("added_by_user_id")
    private UUID addedByUserId;

    @JsonProperty("added_by_user_name")
    private String addedByUserName;

    @JsonProperty("new_member_ids")
    private List<UUID> newMemberIds;

    @JsonProperty("all_member_ids")
    private List<UUID> allMemberIds;
}
