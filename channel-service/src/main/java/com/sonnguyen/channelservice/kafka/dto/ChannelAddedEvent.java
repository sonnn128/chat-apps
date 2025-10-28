package com.sonnguyen.channelservice.kafka.dto;

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
public class ChannelAddedEvent {
    public static final String EVENT_TYPE = "CHANNEL_ADDED";
    
    private UUID channelId;
    private String channelName;
    private UUID addedByUserId;
    private String addedByUserName;
    private List<UUID> addedUserIds;
    private LocalDateTime addedAt;
    private String message; // Notice message content
}
