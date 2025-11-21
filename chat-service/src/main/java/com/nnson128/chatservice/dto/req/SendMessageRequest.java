package com.nnson128.chatservice.dto.req;

import com.nnson128.chatservice.model.ChannelMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
