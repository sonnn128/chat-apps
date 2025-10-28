package com.sonnguyen.presenceservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientConnectedEvent {
    private UUID userId;
    private String sessionId;
    private long timestamp;
}
