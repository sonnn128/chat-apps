package com.sonnguyen.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateChannelResponse {
    private ChannelResponse channel;
    private MessageResponse message;

    public static CreateChannelResponse from(ChannelResponse channel, MessageResponse message) {
        return CreateChannelResponse.builder()
                .channel(channel)
                .message(message)
                .build();
    }
}

