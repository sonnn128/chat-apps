package com.nnson128.relationshipservice.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateDirectChannelRequest {
    private UUID friendId;
}
