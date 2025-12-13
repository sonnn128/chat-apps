package com.nnson128.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.chatapps_base.models.events.EventWrapper;
import com.nnson128.chatapps_base.models.events.message.MessageEventType;
import com.nnson128.chatapps_base.models.events.message.payloads.MessageSentPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelCreatedPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelUpdatedPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.MembersAddedPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestSentPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestAcceptedPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestRejectedPayload;
import com.nnson128.chatapps_base.models.events.call.payloads.CallSignalPayload;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Profile("!ws-only")
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{T(com.nnson128.chatapps_base.constants.KafkaTopics).CHAT_NOTIFICATIONS}", groupId = "notification-group")
    public void handleNotification(String eventJson) {
        try {
            EventWrapper<?> wrapper = objectMapper.readValue(eventJson, EventWrapper.class);
            if (wrapper.getEventType() == null) {
                return;
            }

            processEvent(wrapper.getEventType(), wrapper.getPayload());
        } catch (Exception e) {
        }
    }

    @KafkaListener(topics = "#{T(com.nnson128.chatapps_base.constants.KafkaTopics).FRIENDSHIP_EVENTS}", groupId = "notification-group")
    public void handleFriendshipEvent(String eventJson) {
        try {
            EventWrapper<?> wrapper = objectMapper.readValue(eventJson, EventWrapper.class);
            if (wrapper.getEventType() == null) {
                return;
            }

            processEvent(wrapper.getEventType(), wrapper.getPayload());
        } catch (Exception e) {
        }
    }

    private void processEvent(String eventType, Object payloadData) {
        switch (eventType) {
            case "MESSAGE_SENT":
                handleMessageEvent(convertPayload(payloadData, MessageSentPayload.class));
                break;
            case "CHANNEL_CREATED":
                handleNewChannel(convertPayload(payloadData, ChannelCreatedPayload.class));
                break;
            case "CHANNEL_UPDATED":
                handleChannelUpdated(convertPayload(payloadData, ChannelUpdatedPayload.class));
                break;
            case "MEMBERS_ADDED_TO_CHANNEL":
                handleMembersAddedToChannel(convertPayload(payloadData, MembersAddedPayload.class));
                break;
            case "FRIEND_REQUEST_SENT":
                handleFriendRequestSent(convertPayload(payloadData, FriendRequestSentPayload.class));
                break;
            case "FRIEND_REQUEST_ACCEPTED":
                handleFriendRequestAccepted(convertPayload(payloadData, FriendRequestAcceptedPayload.class));
                break;
            case "FRIEND_REQUEST_REJECTED":
                handleFriendRequestRejected(convertPayload(payloadData, FriendRequestRejectedPayload.class));
                break;
            case "CALL_SIGNAL":
                handleCallSignal(convertPayload(payloadData, CallSignalPayload.class));
                break;
            default:
        }
    }

    private <T> T convertPayload(Object payloadData, Class<T> targetClass) {
        try {
            return objectMapper.convertValue(payloadData, targetClass);
        } catch (Exception e) {
            return null;
        }
    }


    private void handleMessageEvent(MessageSentPayload event) {
        if (event == null) return;

        
        // Convert MessageSentPayload to map with nested key structure for frontend compatibility
        Map<String, Object> key = new HashMap<>();
        key.put("channelId", event.getChannelId());
        key.put("messageId", event.getMessageId());
        
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("key", key);
        wrapper.put("userId", event.getUserId());
        wrapper.put("content", event.getContent());
        wrapper.put("type", event.getType());
        wrapper.put("eventType", event.getEventType().name());
        wrapper.put("senderName", event.getSenderName());
        wrapper.put("senderAvatar", event.getSenderAvatar());
        wrapper.put("timestamp", event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : 0);

        pushToUsers(event.getRecipientIds(), wrapper);
    }

    private void handleNewChannel(ChannelCreatedPayload event) {
        if (event == null) return;
        pushToUsers(event.getMemberIds(), event);
    }

    private void handleMembersAddedToChannel(MembersAddedPayload event) {
        if (event == null) return;
        pushToUsers(event.getAllMemberIds(), event);
    }

    private void handleFriendRequestSent(FriendRequestSentPayload event) {
        if (event == null) return;
        pushToUsers(List.of(event.getRecipientId()), event);
    }

    private void handleFriendRequestAccepted(FriendRequestAcceptedPayload event) {
        if (event == null) return;
        pushToUsers(List.of(event.getFriend1Id(), event.getFriend2Id()), event);
    }

    private void handleFriendRequestRejected(FriendRequestRejectedPayload event) {
        if (event == null) return;
        pushToUsers(List.of(event.getRecipientId()), event);
    }
    
    private void handleChannelUpdated(ChannelUpdatedPayload event) {
        if (event == null) return;
        pushToUsers(event.getMemberIds(), event);
    }

    private void handleCallSignal(CallSignalPayload event) {
        if (event == null || event.getSenderId() == null) return;
        
        // Determine the target: if sender is caller, target is callee; otherwise target is caller.
        UUID targetId = event.getSenderId().equals(event.getCallerId()) ? event.getCalleeId() : event.getCallerId();
        
        if (targetId != null) {
            pushToUsers(List.of(targetId), event);
        }
    }


    private void pushToUsers(List<UUID> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        final String destination = "/queue/notifications";
        Object messagePayload = createMessageWithEventType(payload);

        userIds.forEach(userId -> {
            try {
                messagingTemplate.convertAndSendToUser(userId.toString(), destination, messagePayload);
            } catch (Exception e) {
            }
        });
    }

    private Object createMessageWithEventType(Object payload) {
        try {
            // Convert to JSON string first to preserve field names, then back to map for modification
            String jsonString = objectMapper.writeValueAsString(payload);
            Map<String, Object> messageMap = objectMapper.readValue(jsonString, Map.class);

            if (payload instanceof MessageSentPayload) {
                messageMap.put("eventType", "MESSAGE_SENT");
            } else if (payload instanceof ChannelCreatedPayload) {
                messageMap.put("eventType", "CHANNEL_CREATED");
            } else if (payload instanceof ChannelUpdatedPayload) {
                messageMap.put("eventType", "CHANNEL_UPDATED");
            } else if (payload instanceof MembersAddedPayload) {
                messageMap.put("eventType", "MEMBERS_ADDED_TO_CHANNEL");
            } else if (payload instanceof FriendRequestSentPayload) {
                messageMap.put("eventType", "FRIEND_REQUEST_SENT");
            } else if (payload instanceof FriendRequestAcceptedPayload) {
                messageMap.put("eventType", "FRIEND_REQUEST_ACCEPTED");
            } else if (payload instanceof FriendRequestRejectedPayload) {
                messageMap.put("eventType", "FRIEND_REQUEST_REJECTED");
            } else if (payload instanceof CallSignalPayload) {
                messageMap.put("eventType", "CALL_SIGNAL");
            }

            // Now convert field names from snake_case to camelCase for frontend
            Map<String, Object> transformedMap = new java.util.HashMap<>();
            messageMap.forEach((key, value) -> {
                String camelCaseKey = toCamelCase(key);
                transformedMap.put(camelCaseKey, value);
            });

            return transformedMap;
        } catch (Exception e) {
            return payload;
        }
    }

    private String toCamelCase(String snakeCase) {
        if (!snakeCase.contains("_")) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        String[] parts = snakeCase.split("_");
        result.append(parts[0]);
        
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    result.append(parts[i].substring(1));
                }
            }
        }
        
        return result.toString();
    }
}
