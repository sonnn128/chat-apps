package com.sonnguyen.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.notificationservice.dto.SenderInfo;
import com.sonnguyen.notificationservice.events.EventWrapper;
import com.sonnguyen.notificationservice.events.ChannelCreatedEvent;
import com.sonnguyen.notificationservice.events.MessageSentEvent;
import com.sonnguyen.notificationservice.events.FriendRequestSentEvent;
import com.sonnguyen.notificationservice.events.FriendRequestAcceptedEvent;
import com.sonnguyen.notificationservice.events.FriendRequestRejectedEvent;
import com.sonnguyen.notificationservice.events.MembersAddedToChannelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = "notifications-topic", groupId = "notification-group")
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaHandler
    public void handleNotification(EventWrapper wrapper) {
        String eventType = wrapper.getEventType();

        switch (eventType) {

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
            case FriendRequestSentEvent.EVENT_TYPE -> {
                FriendRequestSentEvent friendRequestEvent =
                        objectMapper.convertValue(wrapper.getPayload(), FriendRequestSentEvent.class);
                handleFriendRequestSent(friendRequestEvent);
            }
            case FriendRequestAcceptedEvent.EVENT_TYPE -> {
                FriendRequestAcceptedEvent friendAcceptedEvent =
                        objectMapper.convertValue(wrapper.getPayload(), FriendRequestAcceptedEvent.class);
                handleFriendRequestAccepted(friendAcceptedEvent);
            }
            case FriendRequestRejectedEvent.EVENT_TYPE -> {
                FriendRequestRejectedEvent friendRejectedEvent =
                        objectMapper.convertValue(wrapper.getPayload(), FriendRequestRejectedEvent.class);
                handleFriendRequestRejected(friendRejectedEvent);
            }
            case MembersAddedToChannelEvent.EVENT_TYPE -> {
                MembersAddedToChannelEvent membersAddedEvent =
                        objectMapper.convertValue(wrapper.getPayload(), MembersAddedToChannelEvent.class);
                handleMembersAddedToChannel(membersAddedEvent);
            }
            default -> log.warn("Unknown event type: {}", wrapper.getEventType());
        }
    }

    private void handleNewChannel(ChannelCreatedEvent event) {
        log.info("✅ NotificationService: CHANNEL_CREATED {}. Notifying {} members, getCreatorId: {}",
                event.getChannelId(), event.getMemberIds().size(), event.getCreatorId());

        // For channel creation, notify all members including creator
        List<UUID> actualRecipients = event.getMemberIds();
        pushToUsers(actualRecipients, event);
    }

    private void handleNewMessage(MessageSentEvent event) {
        log.info("✅ NotificationService: MESSAGE_SENT in channel {}. Notifying {} recipients.",
                event.getKey().getChannelId(), event.getRecipientIds().size());

        // For all messages (including regular chat), notify all recipients including sender
        // This ensures real-time updates for all users in the channel
        List<UUID> actualRecipients = event.getRecipientIds();

        pushToUsers(actualRecipients, event);
    }

    private void handleFriendRequestSent(FriendRequestSentEvent event) {
        log.info("✅ NotificationService: FRIEND_REQUEST_SENT from {} to {}", 
                event.getRequesterId(), event.getFriendId());

        // Notify the friend who received the request
        List<UUID> recipients = List.of(event.getFriendId());
        pushToUsers(recipients, event);
    }

    private void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        log.info("✅ NotificationService: FRIEND_REQUEST_ACCEPTED by {} for {}", 
                event.getAccepterId(), event.getRequesterId());

        // Notify both users about the accepted friendship
        List<UUID> recipients = List.of(event.getRequesterId(), event.getAccepterId());
        pushToUsers(recipients, event);
    }

    private void handleFriendRequestRejected(FriendRequestRejectedEvent event) {
        log.info("✅ NotificationService: FRIEND_REQUEST_REJECTED by {} for {}", 
                event.getRejecterId(), event.getRequesterId());

        // Notify the requester who was rejected
        List<UUID> recipients = List.of(event.getRequesterId());
        pushToUsers(recipients, event);
    }

    private void handleMembersAddedToChannel(MembersAddedToChannelEvent event) {
        log.info("✅ NotificationService: MEMBERS_ADDED_TO_CHANNEL {}. Notifying {} new members.", 
                event.getChannelId(), event.getNewMemberIds().size());

        // Notify only the newly added members (not the person who added them)
        List<UUID> recipients = event.getNewMemberIds();
        pushToUsers(recipients, event);
    }

    private void pushToUsers(List<UUID> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("⚠️ NotificationService: No users to notify");
            return;
        }

        log.info("📤 NotificationService: Pushing payload of type {} to {} users.", payload.getClass().getSimpleName(), userIds.size());
        final String destination = "/queue/notifications";

        // Create a wrapper with eventType for client processing
        Object messagePayload = createMessageWithEventType(payload);

        userIds.forEach(userId -> {
            try {
                log.info("📨 NotificationService: Sending message to userId: {}", userId);
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, messagePayload);
                log.info("✅ NotificationService: Message sent successfully to user: {}", userId);
            } catch (Exception e) {
                log.error("❌ NotificationService: Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }

    private Object createMessageWithEventType(Object payload) {
        try {
            // Convert payload to Map and add eventType
            Map<String, Object> messageMap = objectMapper.convertValue(payload, Map.class);
            
            // Add eventType based on payload class
            if (payload instanceof ChannelCreatedEvent) {
                messageMap.put("eventType", "CHANNEL_CREATED");
            } else if (payload instanceof MessageSentEvent) {
                messageMap.put("eventType", "MESSAGE_SENT");
            } else if (payload instanceof FriendRequestSentEvent) {
                messageMap.put("eventType", "FRIEND_REQUEST_SENT");
            } else if (payload instanceof FriendRequestAcceptedEvent) {
                messageMap.put("eventType", "FRIEND_REQUEST_ACCEPTED");
            } else if (payload instanceof FriendRequestRejectedEvent) {
                messageMap.put("eventType", "FRIEND_REQUEST_REJECTED");
            } else if (payload instanceof MembersAddedToChannelEvent) {
                messageMap.put("eventType", "MEMBERS_ADDED_TO_CHANNEL");
            }
            
            return messageMap;
        } catch (Exception e) {
            log.error("❌ NotificationService: Failed to add eventType to payload: {}", e.getMessage());
            return payload; // Return original payload if conversion fails
        }
    }
}
