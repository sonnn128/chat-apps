package com.sonnguyen.channelservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private MessageKey key;
    private UUID userId;
    private String content;
    private String type;
    private Instant timestamp;
    
    @Data
    @Builder
    public static class MessageKey {
        private UUID channelId;
        private UUID messageId;
    }
}