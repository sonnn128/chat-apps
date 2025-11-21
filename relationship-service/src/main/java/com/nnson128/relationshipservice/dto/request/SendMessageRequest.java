package com.nnson128.relationshipservice.dto.request;

import com.nnson128.relationshipservice.dto.message.ChannelMessageType;
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
