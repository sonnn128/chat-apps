package com.sonnguyen.channelservice.dto.message;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChannelMessageKey {
    private UUID channelId;
    private UUID messageId;
}
