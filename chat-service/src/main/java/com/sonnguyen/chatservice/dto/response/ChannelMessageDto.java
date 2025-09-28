package com.sonnguyen.chatservice.dto.response;

import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMessageDto {
    private ChannelMessageKeyDto key;
    private UUID userId;
    private String content;
    private ChannelMessageType type;
    private Instant timestamp;

    public static ChannelMessageDto from(ChannelMessage channelMessage) {
        return ChannelMessageDto.builder()
                .key(ChannelMessageKeyDto.builder()
                        .channelId(channelMessage.getKey().getChannelId())
                        .messageId(channelMessage.getKey().getMessageId())
                        .build())
                .userId(channelMessage.getUserId())
                .content(channelMessage.getContent())
                .type(channelMessage.getType())
                .timestamp(channelMessage.getTimestamp())
                .build();
    }
}
