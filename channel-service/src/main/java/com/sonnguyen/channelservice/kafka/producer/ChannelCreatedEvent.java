package com.sonnguyen.channelservice.kafka.producer;

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
public class ChannelCreatedEvent {
    public static final String EVENT_TYPE = "CHANNEL_CREATED";

    private UUID channelId;
    private String channelName;
    private LocalDateTime createdAt;

    private UUID creatorId;
    private String creatorName;

    private List<UUID> memberIds;
}
