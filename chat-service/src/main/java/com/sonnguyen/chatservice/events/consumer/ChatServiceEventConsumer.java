package com.sonnguyen.chatservice.events.consumer;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.chatservice.client.ChannelServiceClient;
import com.sonnguyen.chatservice.client.UserServiceClient;
import com.sonnguyen.chatservice.dto.SenderInfo;
import com.sonnguyen.chatservice.dto.response.ApiResponse;
import com.sonnguyen.chatservice.kafka.dto.ChannelCreatedEvent;
import com.sonnguyen.chatservice.kafka.dto.EventWrapper;
import com.sonnguyen.chatservice.kafka.dto.MessageSentEvent;
import com.sonnguyen.chatservice.kafka.dto.MessageSentEventKey;
import com.sonnguyen.chatservice.kafka.dto.AddPeopleEvent;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageKey;
import com.sonnguyen.chatservice.model.ChannelMessageType;
import com.sonnguyen.chatservice.model.UserMessage;
import com.sonnguyen.chatservice.model.UserMessageKey;
import com.sonnguyen.chatservice.repository.ChannelMessageRepository;
import com.sonnguyen.chatservice.repository.UserMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import com.sonnguyen.chatservice.kafka.dto.FriendRequestAcceptedEvent;

@Slf4j
@RequiredArgsConstructor
class ChatServiceEventConsumerDisabled {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final ChannelMessageRepository channelMessageRepository;
    private final UserMessageRepository userMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChannelServiceClient channelServiceClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notifications-topic", groupId = "chat-service-group")
    public void handleNotification(EventWrapper<?> wrapper) {
        log.info("📨 ChatService: Received event type: {}", wrapper.getEventType());

        try {
            switch (wrapper.getEventType()) {
                case ChannelCreatedEvent.EVENT_TYPE -> {
                    ChannelCreatedEvent channelEvent =
                            objectMapper.convertValue(wrapper.getPayload(), ChannelCreatedEvent.class);
                    handleChannelCreated(channelEvent);
                }
                case AddPeopleEvent.EVENT_TYPE -> {
                    AddPeopleEvent addPeopleEvent =
                            objectMapper.convertValue(wrapper.getPayload(), AddPeopleEvent.class);
                    handleAddPeople(addPeopleEvent);
                }
                case MessageSentEvent.EVENT_TYPE -> {
                    MessageSentEvent messageEvent =
                            objectMapper.convertValue(wrapper.getPayload(), MessageSentEvent.class);
                    handleMessageSent(messageEvent);
                }
                case FriendRequestAcceptedEvent.EVENT_TYPE -> {
                    FriendRequestAcceptedEvent event = objectMapper.convertValue(wrapper.getPayload(), FriendRequestAcceptedEvent.class);
                    handleFriendRequestAccepted(event);
                }
                default -> {
                    // Only log unknown event types that are not friend-related
                    if (!wrapper.getEventType().contains("FRIEND")) {
                        log.warn("Unknown event type: {}", wrapper.getEventType());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
            // Don't rethrow to avoid infinite retry loops in Kafka
        }
    }

    private void handleChannelCreated(ChannelCreatedEvent event) {
        createChannelNoticeMessage(event);
    }

    private void handleAddPeople(AddPeopleEvent event) {
        createAddPeopleNoticeMessage(event);
    }

    private void handleMessageSent(MessageSentEvent event) {
        log.info("📨 ChatService: Received MessageSentEvent for channel: {}", event.getKey().getChannelId());
        // MessageSentEvent is already processed by ChannelMessageService
        // This is just for logging and potential future processing
    }

    private void createChannelNoticeMessage(ChannelCreatedEvent event) {
        log.info("🔔 ChatService: Saving notice message for channel: {}", event.getChannelId());

        // Note: Notice message is already created by Channel Service and sent to sender via HTTP
        // Chat Service only needs to save it to database and notify others (exclude sender)
        
        ChannelMessageKey key = new ChannelMessageKey();
        key.setChannelId(event.getChannelId());
        key.setMessageId(Uuids.timeBased()); // Generate new ID for database storage

        String channelName = event.getChannelName() != null ? event.getChannelName() : "kênh";
        String content = "Kênh " + channelName + " đã được tạo thành công";
        log.info("🔔 ChatService: Notice message content: {}", content);

//      1. create a message TYPE notice for database storage
        ChannelMessage noticeMessage = ChannelMessage.builder()
                .key(key)
                .userId(event.getCreatorId())
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .timestamp(Instant.now())
                .build();

//      2. save to channel message
        channelMessageRepository.save(noticeMessage);
        log.info("✅ ChatService: Notice message saved to database for channel: {}", event.getChannelId());

//      3. save to user_message (for all users including sender for consistency)
        saveToUserMessageTable(noticeMessage);

//      4. push event for notification service (exclude sender - sender already got message via HTTP)
        produceNewMessageEvent(noticeMessage, event.getCreatorId());
        log.info("✅ ChatService: Notice message event produced for channel: {} (excluded sender)", event.getChannelId());
    }

    private void createAddPeopleNoticeMessage(AddPeopleEvent event) {
        log.info("🔔 ChatService: Saving add people notice message for channel: {}", event.getChannelId());

        // Note: Notice message is already created by Channel Service and sent to sender via HTTP
        // Chat Service only needs to save it to database and notify others (exclude sender)
        
        ChannelMessageKey key = new ChannelMessageKey();
        key.setChannelId(event.getChannelId());
        key.setMessageId(Uuids.timeBased()); // Generate new ID for database storage

        String memberNames = String.join(", ", event.getNewMemberNames());
        String content = event.getAddedByUserName() + " đã thêm " + memberNames + " vào kênh";
        log.info("🔔 ChatService: Add people notice message content: {}", content);

//      1. create a message TYPE notice for database storage
        ChannelMessage noticeMessage = ChannelMessage.builder()
                .key(key)
                .userId(event.getAddedByUserId())
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .timestamp(Instant.now())
                .build();

//      2. save to channel message
        channelMessageRepository.save(noticeMessage);
        log.info("✅ ChatService: Add people notice message saved to database for channel: {}", event.getChannelId());

//      3. save to user_message (for all users including sender for consistency)
        saveToUserMessageTable(noticeMessage);

//      4. push event for notification service (exclude sender - sender already got message via HTTP)
        produceNewMessageEvent(noticeMessage, event.getAddedByUserId());
        log.info("✅ ChatService: Add people notice message event produced for channel: {} (excluded sender)", event.getChannelId());
    }

    private void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        try {
            log.info("🔔 ChatService: Handling FRIEND_REQUEST_ACCEPTED for requester: {} accepter: {}",
                    event.getRequesterId(), event.getAccepterId());

            // Create a private channel for the two users. Use accepter as creator so they see the immediate notice.
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("channelName", ""); // empty name for direct chat

            ApiResponse<Map<String, Object>> createResp = channelServiceClient.createChannel(createRequest);
            if (createResp == null || !createResp.isSuccess() || createResp.getData() == null) {
                log.warn("⚠️ ChatService: Failed to create channel for friend connection: {}", createResp);
                return;
            }

            Map<String, Object> channelData = createResp.getData();
            Object idObj = channelData.get("id");
            UUID channelId = idObj instanceof String ? UUID.fromString((String) idObj) : (UUID) idObj;

            // Add the requester to the newly created channel
            Map<String, Object> addReq = new HashMap<>();
            addReq.put("memberIds", List.of(event.getRequesterId()));

            ApiResponse<Map<String, Object>> addResp = channelServiceClient.addPeopleToChannel(channelId, addReq);
            if (addResp == null || !addResp.isSuccess()) {
                log.warn("⚠️ ChatService: Failed to add friend to channel: {}", addResp);
            } else {
                log.info("✅ ChatService: Created direct channel {} for users {} and {}",
                        channelId, event.getRequesterId(), event.getAccepterId());
            }

            // Create a NOTICE message for the new direct channel so both users see it in chat
            try {
                ChannelMessageKey key = new ChannelMessageKey();
                key.setChannelId(channelId);
                key.setMessageId(Uuids.timeBased());

                String content = "You are connected on messenger";

                ChannelMessage noticeMessage = ChannelMessage.builder()
                        .key(key)
                        .userId(event.getAccepterId())
                        .content(content)
                        .type(ChannelMessageType.NOTICE)
                        .timestamp(Instant.now())
                        .build();

                // Save to channel_message
                channelMessageRepository.save(noticeMessage);
                log.info("✅ ChatService: Saved friend-connect notice message to DB for channel: {}", channelId);

                // Save to user_message table
                saveToUserMessageTable(noticeMessage);

                // Produce MessageSentEvent to notify both participants (do not exclude anyone)
                produceNewMessageEvent(noticeMessage, null);
                log.info("✅ ChatService: Produced MessageSentEvent for friend-connect notice in channel: {}", channelId);

            } catch (Exception e) {
                log.error("❌ ChatService: Failed to create/publish friend-connect notice: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("❌ ChatService: Error handling FRIEND_REQUEST_ACCEPTED: {}", e.getMessage(), e);
        }
    }

    private void produceNewMessageEvent(ChannelMessage message, UUID excludeUserId) {
        ApiResponse<List<UUID>> response = channelServiceClient.getParticipantIdsByChannelId(message.getKey().getChannelId());
        List<UUID> allRecipientIds = response.getData();
        
        // Exclude sender from recipients (sender already received the message immediately)
        List<UUID> recipientIds = allRecipientIds.stream()
                .filter(userId -> !userId.equals(excludeUserId))
                .toList();

        log.info("📨 ChatService: Sending notice to {} recipients (excluded: {})", 
                recipientIds.size(), excludeUserId);

        // Get sender information for real-time display
        SenderInfo senderInfo = getSenderInfoForNotice();

    MessageSentEvent event = MessageSentEvent.builder()
        .key(MessageSentEventKey.builder()
            .channelId(message.getKey().getChannelId())
            .messageId(message.getKey().getMessageId())
            .build())
        .type(message.getType() != null ? message.getType().name() : null)
        .userId(message.getUserId())
        .content(message.getContent())
        .timestamp(message.getTimestamp())
        .senderName(senderInfo.name())
        .senderAvatar(senderInfo.avatar())
        .recipientIds(recipientIds)
        .build();

        EventWrapper<MessageSentEvent> wrapper = new EventWrapper<>(MessageSentEvent.EVENT_TYPE, event);
        kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
        log.info("✅ ChatService: Produced MessageSentEvent for notice message {} to {} recipients", 
                message.getKey().getMessageId(), recipientIds.size());
    }

    /**
     * Save message to user_message table for efficient user-based queries
     */
    private void saveToUserMessageTable(ChannelMessage message) {
        try {
            UserMessageKey userKey = UserMessageKey.builder()
                    .userId(message.getUserId())
                    .timestamp(message.getTimestamp())
                    .messageId(message.getKey().getMessageId())
                    .channelId(message.getKey().getChannelId())
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .key(userKey)
                    .content(message.getContent())
                    .type(message.getType())
                    .build();

            userMessageRepository.save(userMessage);
            log.info("✅ ChatServiceEventConsumer: Saved notice message to user_message table for user: {}", message.getUserId());
            log.info("🔍 Debug - Saved UserMessage: key={}, content={}, type={}",
                    userMessage.getKey(), userMessage.getContent(), userMessage.getType());
        } catch (Exception e) {
            log.error("❌ ChatServiceEventConsumer: Error saving notice message to user_message table: {}", e.getMessage(), e);
            // Don't rethrow to avoid infinite retry loops in Kafka
        }
    }

    /**
     * Get sender information for notice messages
     */
    private SenderInfo getSenderInfoForNotice() {
        return new SenderInfo("System", null);
    }
}
