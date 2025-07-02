package com.sonnguyen.channelservice.dto.request;

import com.sonnguyen.channelservice.model.ChannelMessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    private UUID channelId;
    private String content;
    private ChannelMessageType type;
}
