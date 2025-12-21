package com.nnson128.relationshipservice.dto.request;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CreateChannelRequest {
    private String channelName;
}