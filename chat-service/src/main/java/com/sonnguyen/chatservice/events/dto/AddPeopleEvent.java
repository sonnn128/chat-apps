package com.sonnguyen.chatservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPeopleEvent {
    public static final String EVENT_TYPE = "ADD_PEOPLE";

    private UUID channelId;
    private String channelName;
    private UUID addedByUserId;
    private String addedByUserName;
    private List<UUID> newMemberIds;
    private List<String> newMemberNames;
    private LocalDateTime addedAt;
}
