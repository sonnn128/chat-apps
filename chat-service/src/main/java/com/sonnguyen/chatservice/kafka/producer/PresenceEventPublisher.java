package com.sonnguyen.chatservice.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PresenceEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PresenceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishConnect(String userId) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "connect");
        msg.put("userId", userId);
        kafkaTemplate.send("presence-events", userId, msg)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.warn("Failed to publish presence connect for {}: {}", userId, ex.getMessage());
                });
    }

    public void publishDisconnect(String userId) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "disconnect");
        msg.put("userId", userId);
        kafkaTemplate.send("presence-events", userId, msg)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.warn("Failed to publish presence disconnect for {}: {}", userId, ex.getMessage());
                });
    }
}
