package com.sonnguyen.channelservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersAddedToChannelEvent {
    private UUID channelId;
    private UUID addedByUserId;
    private String addedByName;
    private List<UUID> newMemberIds;
    private Instant addedAt;
}

