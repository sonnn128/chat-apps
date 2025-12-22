package com.nnson128.chatapps_base.models.events.channel.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.channel.ChannelEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Payload for CHANNEL_CREATED event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreatedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private ChannelEventType eventType;

    @JsonProperty("channel_id")
    private UUID channelId;

    @JsonProperty("channel_name")
    private String channelName;

    @JsonProperty("channel_type")
    private String channelType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("creator_id")
    private UUID creatorId;

    @JsonProperty("creator_name")
    private String creatorName;

    @JsonProperty("member_ids")
    private List<UUID> memberIds;

    @JsonProperty("participants")
    private List<ParticipantInfo> participants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @JsonProperty("user_id")
        private UUID userId;
        
        @JsonProperty("firstname")
        private String firstname;
        
        @JsonProperty("lastname")
        private String lastname;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;
        
        @JsonProperty("role")
        private String role;
    }
}
