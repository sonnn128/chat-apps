package com.sonnguyen.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.notificationservice.events.EventWrapper;
import com.sonnguyen.notificationservice.events.NewChannelCreatedEvent;
import com.sonnguyen.notificationservice.events.NewMessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notifications-topic", groupId = "notification-group")
    public void handleNotification(EventWrapper<?> wrapper) {
        log.info("Received event type: {}", wrapper.getEventType());

        switch (wrapper.getEventType()) {
            case "NEW_MESSAGE" -> {
                NewMessageSentEvent messageEvent =
                        objectMapper.convertValue(wrapper.getPayload(), NewMessageSentEvent.class);
                handleNewMessage(messageEvent);
            }
            case "NEW_CHANNEL" -> {
                NewChannelCreatedEvent channelEvent =
                        objectMapper.convertValue(wrapper.getPayload(), NewChannelCreatedEvent.class);
                handleNewChannel(channelEvent);
            }
            default -> log.warn("Unknown event type: {}", wrapper.getEventType());
        }
    }

    private void handleNewChannel(NewChannelCreatedEvent event) {
        log.info("Event Received: NEW_CHANNEL {}. Notifying {} members, getCreatorId: {}",
                event.getChannelId(), event.getMemberIds().size(), event.getCreatorId());

        List<UUID> actualRecipients = event.getMemberIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getCreatorId()))
                .collect(Collectors.toList());
        pushToUsers(actualRecipients, event);
    }

    private void handleNewMessage(NewMessageSentEvent event) {
        log.info("Event Received: NEW_MESSAGE in channel {}. Notifying {} recipients.",
                event.getKey().getChannelId(), event.getRecipientIds().size());

        List<UUID> actualRecipients = event.getRecipientIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getUserId()))
                .collect(Collectors.toList());

        pushToUsers(actualRecipients, event);
    }

    private void pushToUsers(List<UUID> userIds, Object payload) {
//        if (userIds == null || userIds.isEmpty()) {
//            return;
//        }

        log.info("Pushing payload of type {} to {} users.", payload.getClass().getSimpleName(), userIds.size());
        final String destination = "/queue/notifications";

        userIds.forEach(userId -> {
            try {
                log.info("Sending message to userId: {}", userId);
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
            } catch (Exception e) {
                log.error("Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }
}
