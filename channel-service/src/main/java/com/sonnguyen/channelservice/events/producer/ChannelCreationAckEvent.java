package com.sonnguyen.channelservice.legacy.events.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreationAckEvent {
    private static final String EVENT_TYPE = "CHANNEL_CREATION_ACK";
    
    private UUID channelId;
    private UUID creatorId;
    private boolean noticeMessageCreated;
    private boolean notificationSent;
    private long timestamp;
}
