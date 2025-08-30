package com.sonnguyen.userservice.dto.request;

import com.sonnguyen.userservice.model.message.ChannelMessageType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SendMessageRequest {
    private UUID channelId;
    private String content;
    private ChannelMessageType type;
}
