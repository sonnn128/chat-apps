package com.sonnguyen.chatservice.dto.request;

import com.sonnguyen.chatservice.model.ChannelMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    @NotNull(message = "Channel ID cannot be null")
    private UUID channelId;
    
    @NotBlank(message = "Message content cannot be blank")
    private String content;
    
    private ChannelMessageType type;
}
