package com.sonnguyen.chatservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageSentEventKey {
    private UUID channelId;
    private UUID messageId;
}
