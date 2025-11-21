package com.nnson128.relationshipservice.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMessageDto {
    @JsonProperty("key")
    private ChannelMessageKeyDto key;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private ChannelMessageType type;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("senderName")
    private String senderName;
    
    @JsonProperty("senderAvatar")
    private String senderAvatar;

    public static ChannelMessageDto createNoticeMessage(UUID channelId, UUID messageId, UUID userId, String content) {
        return ChannelMessageDto.builder()
                .key(ChannelMessageKeyDto.builder()
                        .channelId(channelId)
                        .messageId(messageId)
                        .build())
                .userId(userId)
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .timestamp(Instant.now())
                .build();
    }

}
