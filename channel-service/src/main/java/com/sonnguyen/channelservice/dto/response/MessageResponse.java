package com.sonnguyen.channelservice.dto.response;

import com.sonnguyen.channelservice.model.message.ChannelMessageKey;
import com.sonnguyen.channelservice.model.message.ChannelMessageType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private ChannelMessageKey key;
    private UUID userId;
    private String content;
    private ChannelMessageType type;
    private Date timestamp;
}

