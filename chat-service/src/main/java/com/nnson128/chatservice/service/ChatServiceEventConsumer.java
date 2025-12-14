package com.nnson128.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.EventWrapper;
import com.nnson128.chatapps_base.models.events.message.MessageEventType;
import com.nnson128.chatapps_base.models.events.message.payloads.MessageSentPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelCreatedPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.MembersAddedPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestAcceptedPayload;
import com.nnson128.chatservice.client.ChannelServiceClient;
import com.nnson128.chatservice.dto.SenderInfo;
import com.nnson128.chatservice.model.ChannelMessage;
import com.nnson128.chatservice.model.ChannelMessageKey;
import com.nnson128.chatservice.model.ChannelMessageType;
import com.nnson128.chatservice.repository.ChannelMessageRepository;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.nnson128.chatapps_base.dto.res.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatServiceEventConsumer {

    private final ChannelMessageRepository channelMessageRepository;
    private final ChannelServiceClient channelServiceClient;
    private final ObjectMapper objectMapper;
    private final MessageProducerService messageProducerService;

    /**
     * Handle events from CHAT_NOTIFICATIONS topic
     */
    @KafkaListener(topics = "#{T(com.nnson128.chatapps_base.constants.KafkaTopics).CHAT_NOTIFICATIONS}", groupId = "chat-service-group")
    public void handleNotificationEvent(String eventJson) {
        try {
            EventWrapper<?> wrapper = objectMapper.readValue(eventJson, EventWrapper.class);
            if (wrapper.getEventType() == null) {
                return;
            }

            processNotificationEvent(wrapper.getEventType(), wrapper.getPayload());
        } catch (Exception e) {
            log.error("ChatServiceEventConsumer: Error processing notification event: {}", e.getMessage());
        }
    }

    private void processNotificationEvent(String eventType, Object payloadData) {
        switch (eventType) {
            case "MESSAGE_SENT":
                // MESSAGE_SENT events are already persisted by ChannelMessageService
                // and only need real-time notification which is handled by NotificationService
                break;
            case "CHANNEL_CREATED":
                handleChannelCreated(convertPayload(payloadData, ChannelCreatedPayload.class));
                break;
            case "MEMBERS_ADDED_TO_CHANNEL":
                handleAddPeople(convertPayload(payloadData, MembersAddedPayload.class));
                break;
            default:
                System.out.println("⚠️ ChatServiceEventConsumer: Unknown notification event type: " + eventType);
        }
    }

    /**
     * Handle events from FRIENDSHIP_EVENTS topic
     */
    @KafkaListener(topics = "#{T(com.nnson128.chatapps_base.constants.KafkaTopics).FRIENDSHIP_EVENTS}", groupId = "chat-service-group")
    public void handleFriendshipEvent(String eventJson) {
        try {
            EventWrapper<?> wrapper = objectMapper.readValue(eventJson, EventWrapper.class);
            if (wrapper.getEventType() == null) {
                System.out.println("⚠️ ChatServiceEventConsumer: Friendship event has no eventType");
                return;
            }

            processFriendshipEvent(wrapper.getEventType(), wrapper.getPayload());
        } catch (Exception e) {
            System.out.println("❌ ChatServiceEventConsumer: Error processing friendship event: " + e.getMessage());
        }
    }

    private void processFriendshipEvent(String eventType, Object payloadData) {
        switch (eventType) {
            case "FRIEND_REQUEST_ACCEPTED":
                handleFriendRequestAccepted(convertPayload(payloadData, FriendRequestAcceptedPayload.class));
                break;
            default:
                System.out.println("⚠️ ChatServiceEventConsumer: Unknown friendship event type: " + eventType);
        }
    }

    private <T> T convertPayload(Object payloadData, Class<T> targetClass) {
        try {
            return objectMapper.convertValue(payloadData, targetClass);
        } catch (Exception e) {
            System.out.println("❌ ChatServiceEventConsumer: Failed to convert payload to " + targetClass.getSimpleName());
            return null;
        }
    }

    private void handleChannelCreated(ChannelCreatedPayload event) {
        // Save the notice message for channel creation to database
        createChannelNoticeMessage(event);
    }

    private void handleAddPeople(MembersAddedPayload event) {
        // Save the notice message for adding members to database
        createAddPeopleNoticeMessage(event);
    }

    private void createChannelNoticeMessage(ChannelCreatedPayload event) {
        ChannelMessageKey key = ChannelMessageKey.builder()
                .channelId(event.getChannelId())
                .messageId(Uuids.timeBased())
                .build();

        String channelName = event.getChannelName() != null ? event.getChannelName() : "kênh";
        String content = "Kênh " + channelName + " đã được tạo thành công";

        ChannelMessage noticeMessage = ChannelMessage.builder()
                .key(key)
                .userId(event.getCreatorId())
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .timestamp(Instant.now())
                .build();

        channelMessageRepository.save(noticeMessage);
        produceNewMessageEvent(noticeMessage, event.getCreatorId());
    }

    private void createAddPeopleNoticeMessage(MembersAddedPayload event) {
        ChannelMessageKey key = ChannelMessageKey.builder()
                .channelId(event.getChannelId())
                .messageId(Uuids.timeBased())
                .build();

        // Build member count string for notice message
        int memberCount = event.getNewMemberIds() != null ? event.getNewMemberIds().size() : 0;
        String memberCountText = memberCount == 1 ? "1 người" : memberCount + " người";
        String addedByName = event.getAddedByUserName() != null ? event.getAddedByUserName() : "A user";
        String content = addedByName + " đã thêm " + memberCountText + " vào kênh";

        ChannelMessage noticeMessage = ChannelMessage.builder()
                .key(key)
                .userId(event.getAddedByUserId())
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .timestamp(Instant.now())
                .build();

        channelMessageRepository.save(noticeMessage);
        produceNewMessageEvent(noticeMessage, event.getAddedByUserId());
    }

    private void handleFriendRequestAccepted(FriendRequestAcceptedPayload event) {
        try {
            Map<String, Object> directChannelReq = new HashMap<>();
            directChannelReq.put("friendId", event.getFriend1Id()); // friend1 is the requester, friend2 is the accepter (current user context in client call)

            Map<String, Object> request = new HashMap<>();
            request.put("friendId", event.getFriend1Id()); // Assuming context is friend2? Or does it matter?
            // If the call fails, we catch exception.
            
            // Wait, if I use `createDirectChannel`, I don't need to add people.
            ApiResponse<Map<String, Object>> response = channelServiceClient.createDirectChannel(request);
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> channelData = response.getData();
                Object idObj = channelData.get("id");
                UUID channelId = idObj instanceof String ? UUID.fromString((String) idObj) : (UUID) idObj;
                
                // Send the notice message
                 try {
                    ChannelMessageKey key = ChannelMessageKey.builder()
                            .channelId(channelId)
                            .messageId(Uuids.timeBased())
                            .build();

                    String content = "You are connected on messenger";

                    ChannelMessage noticeMessage = ChannelMessage.builder()
                            .key(key)
                            .userId(event.getFriend2Id()) // We attribute the message to friend2
                            .content(content)
                            .type(ChannelMessageType.NOTICE)
                            .timestamp(Instant.now())
                            .build();

                    channelMessageRepository.save(noticeMessage);
                    produceNewMessageEvent(noticeMessage, null);

                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            log.error("Error handling friend request accepted: {}", e.getMessage());
        }
    }

    private void produceNewMessageEvent(ChannelMessage message, UUID excludeUserId) {
        ApiResponse<List<UUID>> response = channelServiceClient.getParticipantIdsByChannelId(message.getKey().getChannelId());
        List<UUID> allRecipientIds = response.getData();

        List<UUID> recipientIds = allRecipientIds.stream()
                .filter(userId -> !userId.equals(excludeUserId))
                .toList();

        SenderInfo senderInfo = getSenderInfoForNotice();

        // Create MessageSentPayload event for notification
        MessageSentPayload event = MessageSentPayload.builder()
                .eventType(MessageEventType.MESSAGE_SENT)
                .eventId(UUID.randomUUID().toString())
                .messageId(message.getKey().getMessageId().toString())
                .channelId(message.getKey().getChannelId().toString())
                .type(message.getType() != null ? message.getType().name() : null)
                .content(message.getContent())
                .userId(message.getUserId())
                .senderName(senderInfo.name())
                .senderAvatar(senderInfo.avatar())
                .timestamp(message.getTimestamp())
                .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, MessageEventType.MESSAGE_SENT.name(), event, null, null);
        } catch (Exception e) {
        }
    }

    private SenderInfo getSenderInfoForNotice() {
        return new SenderInfo("System", null);
    }
}
