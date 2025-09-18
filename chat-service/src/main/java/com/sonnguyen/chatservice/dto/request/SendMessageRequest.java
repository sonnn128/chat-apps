package com.sonnguyen.chatservice.dto.request;

import com.sonnguyen.chatservice.model.ChannelMessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    private UUID channelId;
    private String content;
    private ChannelMessageType type;
}
