package com.sonnguyen.userservice.model.message;

import lombok.Data;

import java.util.UUID;

@Data
public class ChannelMessageKey {
    private UUID channelId;
    private UUID messageId;
}
