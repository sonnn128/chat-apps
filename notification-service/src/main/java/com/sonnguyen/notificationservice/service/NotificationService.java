package com.sonnguyen.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.notificationservice.events.EventWrapper;
import com.sonnguyen.notificationservice.events.ChannelCreatedEvent;
import com.sonnguyen.notificationservice.events.MessageSentEvent;
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
        log.info("📨 NotificationService: Received event type: {}", wrapper.getEventType());

        switch (wrapper.getEventType()) {
            case MessageSentEvent.EVENT_TYPE -> {
                MessageSentEvent messageEvent =
                        objectMapper.convertValue(wrapper.getPayload(), MessageSentEvent.class);
                handleNewMessage(messageEvent);
            }
            case ChannelCreatedEvent.EVENT_TYPE -> {
                ChannelCreatedEvent channelEvent =
                        objectMapper.convertValue(wrapper.getPayload(), ChannelCreatedEvent.class);
                handleNewChannel(channelEvent);
            }
            default -> log.warn("Unknown event type: {}", wrapper.getEventType());
        }
    }

    private void handleNewChannel(ChannelCreatedEvent event) {
        log.info("✅ NotificationService: CHANNEL_CREATED {}. Notifying {} members, getCreatorId: {}",
                event.getChannelId(), event.getMemberIds().size(), event.getCreatorId());

        List<UUID> actualRecipients = event.getMemberIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getCreatorId()))
                .collect(Collectors.toList());
        pushToUsers(actualRecipients, event);
    }

    private void handleNewMessage(MessageSentEvent event) {
        log.info("✅ NotificationService: MESSAGE_SENT in channel {}. Notifying {} recipients.",
                event.getKey().getChannelId(), event.getRecipientIds().size());

        List<UUID> actualRecipients = event.getRecipientIds().stream()
                .filter(recipientId -> !recipientId.equals(event.getUserId()))
                .collect(Collectors.toList());

        pushToUsers(actualRecipients, event);
    }

    private void pushToUsers(List<UUID> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("⚠️ NotificationService: No users to notify");
            return;
        }

        log.info("📤 NotificationService: Pushing payload of type {} to {} users.", payload.getClass().getSimpleName(), userIds.size());
        final String destination = "/queue/notifications";

        userIds.forEach(userId -> {
            try {
                log.info("📨 NotificationService: Sending message to userId: {}", userId);
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
                log.info("✅ NotificationService: Message sent successfully to user: {}", userId);
            } catch (Exception e) {
                log.error("❌ NotificationService: Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }
}
