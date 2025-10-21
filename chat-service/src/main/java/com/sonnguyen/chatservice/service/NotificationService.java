package com.sonnguyen.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.chatservice.events.dto.ChannelCreatedEvent;
import com.sonnguyen.chatservice.events.dto.EventWrapper;
import com.sonnguyen.chatservice.events.dto.MessageSentEvent;
import com.sonnguyen.chatservice.events.dto.AddPeopleEvent;
import com.sonnguyen.chatservice.events.dto.FriendRequestAcceptedEvent;
import com.sonnguyen.chatservice.events.dto.FriendRequestRejectedEvent;
import com.sonnguyen.chatservice.events.dto.FriendRequestSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = "notifications-topic", groupId = "notification-group")
@Profile("!ws-only")
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaHandler
    public void handleNotification(EventWrapper<?> wrapper) {
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
            case AddPeopleEvent.EVENT_TYPE -> {
                AddPeopleEvent addPeopleEvent =
                        objectMapper.convertValue(wrapper.getPayload(), AddPeopleEvent.class);
                handleMembersAddedToChannel(addPeopleEvent);
            }
            case FriendRequestSentEvent.EVENT_TYPE -> {
                FriendRequestSentEvent event = objectMapper.convertValue(wrapper.getPayload(), FriendRequestSentEvent.class);
                handleFriendRequestSent(event);
            }
            case FriendRequestAcceptedEvent.EVENT_TYPE -> {
                FriendRequestAcceptedEvent event = objectMapper.convertValue(wrapper.getPayload(), FriendRequestAcceptedEvent.class);
                handleFriendRequestAccepted(event);
            }
            case FriendRequestRejectedEvent.EVENT_TYPE -> {
                FriendRequestRejectedEvent event = objectMapper.convertValue(wrapper.getPayload(), FriendRequestRejectedEvent.class);
                handleFriendRequestRejected(event);
            }
            default -> log.warn("Unknown event type: {}", wrapper.getEventType());
        }
    }

    private void handleNewChannel(ChannelCreatedEvent event) {
        log.info("✅ Notification: CHANNEL_CREATED {}. Notifying {} members, creator: {}",
                event.getChannelId(), event.getMemberIds().size(), event.getCreatorId());
        pushToUsers(event.getMemberIds(), event);
    }

    private void handleNewMessage(MessageSentEvent event) {
        log.info("✅ Notification: MESSAGE_SENT in channel {}. Notifying {} recipients.",
                event.getKey().getChannelId(), event.getRecipientIds().size());
        pushToUsers(event.getRecipientIds(), event);
    }

    private void handleMembersAddedToChannel(AddPeopleEvent event) {
        log.info("✅ Notification: ADD_PEOPLE {}. Notifying {} new members.",
                event.getChannelId(), event.getNewMemberIds().size());
        pushToUsers(event.getNewMemberIds(), event);
    }

    private void handleFriendRequestSent(FriendRequestSentEvent event) {
        log.info("✅ Notification: FRIEND_REQUEST_SENT from {} to {}", event.getRequesterId(), event.getFriendId());
        pushToUsers(List.of(event.getFriendId()), event);
    }

    private void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        log.info("✅ Notification: FRIEND_REQUEST_ACCEPTED by {} for {}", event.getAccepterId(), event.getRequesterId());
        pushToUsers(List.of(event.getRequesterId(), event.getAccepterId()), event);
    }

    private void handleFriendRequestRejected(FriendRequestRejectedEvent event) {
        log.info("✅ Notification: FRIEND_REQUEST_REJECTED by {} for {}", event.getRejecterId(), event.getRequesterId());
        pushToUsers(List.of(event.getRequesterId()), event);
    }

    private void pushToUsers(List<UUID> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("⚠️ Notification: No users to notify");
            return;
        }

        final String destination = "/queue/notifications";
        Object messagePayload = createMessageWithEventType(payload);

        userIds.forEach(userId -> {
            try {
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, messagePayload);
            } catch (Exception e) {
                log.error("❌ Notification: Failed to push payload to user {}. Error: {}", userId, e.getMessage());
            }
        });
    }

    private Object createMessageWithEventType(Object payload) {
        try {
            Map<String, Object> messageMap = objectMapper.convertValue(payload, Map.class);

            if (payload instanceof ChannelCreatedEvent) {
                messageMap.put("eventType", ChannelCreatedEvent.EVENT_TYPE);
            } else if (payload instanceof MessageSentEvent) {
                messageMap.put("eventType", MessageSentEvent.EVENT_TYPE);
            } else if (payload instanceof AddPeopleEvent) {
                messageMap.put("eventType", AddPeopleEvent.EVENT_TYPE);
            } else if (payload instanceof FriendRequestSentEvent) {
                messageMap.put("eventType", FriendRequestSentEvent.EVENT_TYPE);
            } else if (payload instanceof FriendRequestAcceptedEvent) {
                messageMap.put("eventType", FriendRequestAcceptedEvent.EVENT_TYPE);
            } else if (payload instanceof FriendRequestRejectedEvent) {
                messageMap.put("eventType", FriendRequestRejectedEvent.EVENT_TYPE);
            }

            return messageMap;
        } catch (Exception e) {
            log.error("❌ Notification: Failed to add eventType to payload: {}", e.getMessage());
            return payload;
        }
    }
}
