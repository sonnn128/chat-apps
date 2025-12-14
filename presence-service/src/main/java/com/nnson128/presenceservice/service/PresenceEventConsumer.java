package com.nnson128.presenceservice.service;

import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.user.UserEventType;
import com.nnson128.chatapps_base.models.events.user.payloads.UserStatusChangedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceEventConsumer {

    private final PresenceService presenceService;

    /**
     * Listens for user status change events (ONLINE, OFFLINE, etc.)
     * Spring Boot will automatically parse JSON payload to UserStatusChangedPayload.
     */
    @KafkaListener(
        topics = KafkaTopics.USER_STATUS_CHANGED,
        groupId = "presence-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserStatusChanged(UserStatusChangedPayload event) {
        try {
            if (event == null || event.getUserId() == null) {
                return;
            }

            String userIdStr = event.getUserId().toString();
            String status = event.getStatus();

            if ("ONLINE".equalsIgnoreCase(status) || UserEventType.USER_ONLINE.equals(event.getEventType())) {
                presenceService.connect(userIdStr);
            } else if ("OFFLINE".equalsIgnoreCase(status) || UserEventType.USER_OFFLINE.equals(event.getEventType())) {
                presenceService.disconnect(userIdStr);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
