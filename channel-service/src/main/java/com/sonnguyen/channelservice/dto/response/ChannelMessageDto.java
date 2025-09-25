package com.sonnguyen.channelservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ChannelMessageDto {
    @JsonProperty("key")
    private MessageKeyDto key;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @Data
    public static class MessageKeyDto {
        @JsonProperty("channelId")
        private UUID channelId;
        
        @JsonProperty("messageId")
        private UUID messageId;
    }
}
