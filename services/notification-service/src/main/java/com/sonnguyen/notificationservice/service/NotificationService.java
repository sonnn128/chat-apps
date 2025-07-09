package com.sonnguyen.notificationservice.service;

import com.sonnguyen.notificationservice.events.NewChannelCreatedEvent;
import com.sonnguyen.notificationservice.events.NewMessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = {"new-messages-topic", "new-channels-topic"}, groupId = "notification-group")
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaHandler
    public void handleNewMessage(NewMessageSentEvent event) {
        log.info("Event Received: NEW_MESSAGE in channel {}. Notifying {} recipients.",
                event.getKey().getChannelId(), event.getRecipientIds().size());

        List<UUID> actualRecipients = event.getRecipientIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getUserId()))
                .collect(Collectors.toList());

        pushToUsers(actualRecipients, event);
    }

    @KafkaHandler
    public void handleNewChannel(NewChannelCreatedEvent event) {
        log.info("Event Received: NEW_CHANNEL {}. Notifying {} members.",
                event.getChannelId(), event.getMemberIds().size());

        List<UUID> actualRecipients = event.getMemberIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getCreatorId()))
                .collect(Collectors.toList());
        
        pushToUsers(actualRecipients, event);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object object) {
        log.warn("Received an unknown event type from Kafka: {}", object);
        log.warn("Received an unknown event type from Kafka: {}", object.getClass());
        log.warn("Received an unknown event type from Kafka: {}", object.getClass().getName());
    }

    private void pushToUsers(List<UUID> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        log.debug("Pushing payload of type {} to {} users.", payload.getClass().getSimpleName(), userIds.size());
        final String destination = "/queue/notifications";

        userIds.forEach(userId -> {
            try {
                log.info("Sending message to userId: {}", userId);
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
//                "/user/{user-id}/queue/notifications"
            } catch (Exception e) {
                log.error("Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }
}