package com.sonnguyen.notificationservice.service;

import com.sonnguyen.notificationservice.events.dto.NewChannelCreatedEvent;
import com.sonnguyen.notificationservice.events.dto.NewMessageSentEvent;
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
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaHandler
    public void handleNewMessage(NewMessageSentEvent event) {
        log.info("Event Received: NEW_MESSAGE in channel {}. Notifying {} recipients.",
                event.getChannelId(), event.getRecipientIds().size());

        List<UUID> actualRecipients = event.getRecipientIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getSenderId()))
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
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
            } catch (Exception e) {
                log.error("Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }
}