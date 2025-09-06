package com.sonnguyen.channelservice.dto.response;

import com.sonnguyen.channelservice.model.Channel;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChannelResponse {
    private UUID id;
    private String channelName;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private List<MessageResponse> messages;
    private List<ChannelParticipant> participants;

    public static ChannelResponse fromChannelAndMessage(ChannelResponse channelResponse, List<MessageResponse> messages) {
        return ChannelResponse.builder()
                .id(channelResponse.getId())
                .channelName(channelResponse.getChannelName())
                .createdBy(channelResponse.getCreatedBy())
                .createdAt(channelResponse.getCreatedAt())
                .messages(messages)
                .build();
    }
    public static ChannelResponse from(Channel channel){
        return ChannelResponse.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .createdBy(channel.getCreatedBy())
                .createdAt(channel.getCreatedAt())
                .build();
    }
    public static ChannelResponse fromChannelAndMessage(ChannelResponse channelResponse,
                                                        List<MessageResponse> messages,
                                                        List<ChannelParticipant> participants) {
        return ChannelResponse.builder()
                .id(channelResponse.getId())
                .channelName(channelResponse.getChannelName())
                .createdBy(channelResponse.getCreatedBy())
                .createdAt(channelResponse.getCreatedAt())
                .messages(messages)
                .participants(participants)
                .build();
    }

}
