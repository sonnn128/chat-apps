package com.sonnguyen.chatservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPeopleEvent {
    public static final String EVENT_TYPE = "ADD_PEOPLE";

    private UUID channelId;
    private UUID addedByUserId;
    private String addedByUserName;
    private List<UUID> newMemberIds;
    private List<String> newMemberNames;
    private Instant createdAt;
}
