package com.sonnguyen.channelservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChannelResponse {
    private UUID id;
    private String channelName;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
