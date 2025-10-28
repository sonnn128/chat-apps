package com.sonnguyen.presenceservice.kafka.consumer;

import com.sonnguyen.presenceservice.service.PresenceService;
import com.sonnguyen.presenceservice.kafka.dto.ClientConnectedEvent;
import com.sonnguyen.presenceservice.kafka.dto.ClientDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventConsumer {

    private final PresenceService presenceService;
    // Bỏ ObjectMapper đi, Spring Boot sẽ tự động deserialize JSON -> DTO

    /**
     * Chỉ lắng nghe sự kiện client kết nối.
     * Spring Boot sẽ tự động parse JSON payload thành ClientConnectedEvent.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.client-connected}",
            groupId = "presence-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClientConnected(ClientConnectedEvent event) {
        if (event == null || event.getUserId() == null) {
            log.warn("Received invalid ClientConnectedEvent: event or userId is null");
            return;
        }

    String userIdStr = event.getUserId().toString();
    log.info("📨 PresenceService: User connected: {}", userIdStr);
    presenceService.connect(userIdStr);

    }

    /**
     * Chỉ lắng nghe sự kiện client ngắt kết nối.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.client-disconnected}",
            groupId = "presence-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClientDisconnected(ClientDisconnectedEvent event) {
        try {
            if (event == null || event.getUserId() == null) {
                log.warn("Received invalid ClientDisconnectedEvent: event or userId is null");
                return;
            }

            String userIdStr = event.getUserId().toString();
            log.info("📨 PresenceService: User disconnected: {}", userIdStr);
            presenceService.disconnect(userIdStr);

        } catch (Exception ex) {
            log.warn("Failed to process disconnect event for user {}: {}", event != null && event.getUserId() != null ? event.getUserId().toString() : "<null>", ex.getMessage());
        }
    }
}