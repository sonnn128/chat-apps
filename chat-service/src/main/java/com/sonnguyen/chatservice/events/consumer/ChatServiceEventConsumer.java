package com.sonnguyen.chatservice.events.consumer;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.chatservice.client.ChannelServiceClient;
import com.sonnguyen.chatservice.dto.response.ApiResponse;
import com.sonnguyen.chatservice.exception.ExternalServiceException;
import com.sonnguyen.chatservice.events.dto.ChannelCreatedEvent;
import com.sonnguyen.chatservice.events.dto.EventWrapper;
import com.sonnguyen.chatservice.events.dto.MessageSentEvent;
import com.sonnguyen.chatservice.events.dto.MessageSentEventKey;
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
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatServiceEventConsumer {

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
                case MessageSentEvent.EVENT_TYPE -> {
                    MessageSentEvent messageEvent =
                            objectMapper.convertValue(wrapper.getPayload(), MessageSentEvent.class);
                    handleMessageSent(messageEvent);
                }
                default -> log.warn("Unknown event type: {}", wrapper.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
            // Don't rethrow to avoid infinite retry loops in Kafka
        }
    }

    private void handleChannelCreated(ChannelCreatedEvent event) {
        log.info("Received ChannelCreatedEvent: {}", event);
        createChannelNoticeMessage(event);
    }

    private void handleMessageSent(MessageSentEvent event) {
        log.info("📨 ChatService: Received MessageSentEvent for channel: {}", event.getKey().getChannelId());
        // MessageSentEvent is already processed by ChannelMessageService
        // This is just for logging and potential future processing
    }

    private void createChannelNoticeMessage(ChannelCreatedEvent event) {
        try {
            log.info("Creating notice message for new channel: {}", event.getChannelId());
            
            ChannelMessageKey key = new ChannelMessageKey();
            key.setChannelId(event.getChannelId());
            key.setMessageId(Uuids.timeBased());

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
            log.info("Notice message created for new channel: {}", event.getChannelId());
            
            // Also save to user_message table for efficient user-based queries
            saveToUserMessageTable(noticeMessage);
            
            // Publish notification event for notification-service
            produceNewMessageEvent(noticeMessage);
            
            log.info("✅ Channel creation process completed for channel: {}", event.getChannelId());
            
        } catch (Exception e) {
            log.error("Error creating notice message: {}", e.getMessage(), e);
            // Don't rethrow to avoid infinite retry loops in Kafka
        }
    }

    private void produceNewMessageEvent(ChannelMessage message) {
        try {
            ApiResponse<List<UUID>> response = channelServiceClient.getParticipantIdsByChannelId(message.getKey().getChannelId());
            List<UUID> recipientIds = response.getData();

            MessageSentEvent event = MessageSentEvent.builder()
                    .key(MessageSentEventKey.builder()
                            .channelId(message.getKey().getChannelId())
                            .messageId(message.getKey().getMessageId())
                            .build())
                    .type(message.getType())
                    .userId(message.getUserId())
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .recipientIds(recipientIds)
                    .build();

            EventWrapper<MessageSentEvent> wrapper = new EventWrapper<>(MessageSentEvent.EVENT_TYPE, event);
            kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
            log.info("Produced MessageSentEvent for notice message {}", message.getKey().getMessageId());
            
        } catch (Exception e) {
            log.error("Error producing MessageSentEvent: {}", e.getMessage(), e);
            // Don't rethrow to avoid infinite retry loops in Kafka
        }
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
}
