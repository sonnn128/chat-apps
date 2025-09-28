package com.sonnguyen.channelservice.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMessageKeyDto {
    @JsonProperty("channelId")
    private UUID channelId;
    
    @JsonProperty("messageId")
    private UUID messageId;
}
