package com.sonnguyen.chatservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembersAddedToChannelEvent {
    public static final String EVENT_TYPE = "MEMBERS_ADDED_TO_CHANNEL";

    private UUID channelId;
    private List<UUID> addedMemberIds;
    private UUID addedBy;
}
