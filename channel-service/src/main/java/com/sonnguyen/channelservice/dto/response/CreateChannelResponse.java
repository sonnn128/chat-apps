package com.sonnguyen.channelservice.dto.response;

import com.sonnguyen.channelservice.model.ChannelParticipant;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CreateChannelResponse {
    private ChannelResponse channel;
    private MessageResponse message;
    private Set<ChannelParticipant> participants;

    public static CreateChannelResponse from(ChannelResponse channel, MessageResponse message, Set<ChannelParticipant> participants) {
        return CreateChannelResponse.builder()
                .channel(channel)
                .message(message)
                .participants(participants)
                .build();
    }
}

