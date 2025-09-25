package com.sonnguyen.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.chatservice.client.ChannelServiceClient;
import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.dto.response.ApiResponse;
import com.sonnguyen.chatservice.exception.ChannelAccessException;
import com.sonnguyen.chatservice.exception.ExternalServiceException;
import com.sonnguyen.chatservice.exception.InvalidMessageException;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelMessageService {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final ChannelMessageRepository channelMessageRepository;
    private final UserMessageRepository userMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChannelServiceClient channelServiceClient;

    public List<ChannelMessage> getAllMessagesOfChannel(UUID channelId) {
        if (channelId == null) {
            throw new InvalidMessageException("Channel ID cannot be null");
        }
        
        try {
            return channelMessageRepository.findAllByKeyChannelIdOrderByKeyMessageIdAsc(channelId);
        } catch (Exception e) {
            log.error("Error retrieving messages for channel {}: {}", channelId, e.getMessage(), e);
            throw new ExternalServiceException("Failed to retrieve messages for channel: " + channelId, e);
        }
    }

    public ChannelMessage saveMessage(SendMessageRequest request, UUID senderId) {
        validateMessageRequest(request, senderId);
        authorizeUserForChannel(request.getChannelId(), senderId);

        try {
            ChannelMessageKey key = new ChannelMessageKey();
            key.setChannelId(request.getChannelId());
            key.setMessageId(Uuids.timeBased());

            ChannelMessage messageToSave = ChannelMessage.builder()
                    .key(key)
                    .userId(senderId)
                    .content(request.getContent())
                    .type(request.getType() != null ? request.getType() : ChannelMessageType.CHAT)
                    .timestamp(Instant.now())
                    .build();
            
            // Save to channel_message table
            ChannelMessage savedMessage = channelMessageRepository.save(messageToSave);
            
            // Also save to user_message table for efficient user-based queries
            saveToUserMessageTable(savedMessage);
            
            return savedMessage;
        } catch (Exception e) {
            log.error("Error saving message: {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to save message", e);
        }
    }

    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {
        ChannelMessage savedMessage = saveMessage(request, senderId);
        produceNewMessageEvent(savedMessage);
        return savedMessage;
    }

    private void validateMessageRequest(SendMessageRequest request, UUID senderId) {
        if (request == null) {
            throw new InvalidMessageException("Message request cannot be null");
        }
        if (request.getChannelId() == null) {
            throw new InvalidMessageException("Channel ID cannot be null");
        }
        if (senderId == null) {
            throw new InvalidMessageException("Sender ID cannot be null");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }
        if (request.getContent().length() > 1000) {
            throw new InvalidMessageException("Message content cannot exceed 1000 characters");
        }
    }

    private void authorizeUserForChannel(UUID channelId, UUID userId) {
        try {
            ApiResponse<Boolean> response = channelServiceClient.checkUserIsParticipant(channelId, userId);
            if (!response.isSuccess() || !response.getData()) {
                throw new ChannelAccessException("User is not a participant in this channel");
            }
        } catch (Exception e) {
            if (e instanceof ChannelAccessException) {
                throw e;
            }
            log.error("Error checking user authorization for channel {}: {}", channelId, e.getMessage(), e);
            throw new ExternalServiceException("Failed to verify user authorization", e);
        }
    }

    private void produceNewMessageEvent(ChannelMessage message) {
        try {
            ApiResponse<List<UUID>> response = channelServiceClient.getParticipantIdsByChannelId(message.getKey().getChannelId());
            if (!response.isSuccess() || response.getData() == null) {
                log.error("Failed to get participant IDs for channel {}", message.getKey().getChannelId());
                return;
            }
            
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
            log.info("event: " + event);

            EventWrapper<MessageSentEvent> wrapper =
                    new EventWrapper<>(MessageSentEvent.EVENT_TYPE, event);

            kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
            log.info("Produced MessageSentEvent for message {}", message.getKey().getMessageId());
        } catch (Exception e) {
            log.error("Error producing message event for message {}: {}", message.getKey().getMessageId(), e.getMessage(), e);
            // Don't throw exception here as message is already saved
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
            log.info("✅ ChannelMessageService: Saved message to user_message table for user: {}", message.getUserId());
        } catch (Exception e) {
            log.error("❌ ChannelMessageService: Error saving to user_message table: {}", e.getMessage(), e);
            // Don't throw exception here as the main message is already saved
        }
    }

    /**
     * Get all messages sent by a specific user across all channels
     * This is optimized for initial UI rendering - no need to call channel-service API
     */
    public Map<UUID, List<ChannelMessage>> getAllMessagesByUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidMessageException("User ID cannot be null");
        }
        
        log.info("📨 ChannelMessageService: Getting all messages sent by user: {}", userId);
        
        try {
            // Query from user_message table (more efficient)
            List<UserMessage> userMessages = userMessageRepository.findAllByKeyUserIdOrderByKeyTimestampDesc(userId);
            
            if (userMessages == null || userMessages.isEmpty()) {
                log.info("📭 ChannelMessageService: No messages found for user: {}", userId);
                return Map.of();
            }
            
            log.info("📋 ChannelMessageService: Found {} messages for user: {}", userMessages.size(), userId);
            
            // Debug: Log the first user message to see its structure
            if (!userMessages.isEmpty()) {
                UserMessage firstMessage = userMessages.get(0);
                log.info("🔍 Debug - First UserMessage: key={}, content={}, type={}", 
                        firstMessage.getKey(), firstMessage.getContent(), firstMessage.getType());
            }
            
            // Convert UserMessage to ChannelMessage and group by channelId
            return userMessages.stream()
                    .map(this::convertToChannelMessage)
                    .collect(Collectors.groupingBy(
                            message -> message.getKey().getChannelId(),
                            Collectors.toList()
                    ));
            
        } catch (Exception e) {
            log.error("❌ ChannelMessageService: Error getting all messages for user {}: {}", userId, e.getMessage(), e);
            throw new ExternalServiceException("Failed to retrieve messages for user: " + userId, e);
        }
    }
    
    /**
     * Convert UserMessage to ChannelMessage
     */
    private ChannelMessage convertToChannelMessage(UserMessage userMessage) {
        log.info("🔍 Debug - Converting UserMessage: key={}, content={}, type={}", 
                userMessage.getKey(), userMessage.getContent(), userMessage.getType());
        
        if (userMessage.getKey() == null) {
            log.error("❌ UserMessage key is null!");
            return null;
        }
        
        ChannelMessageKey key = new ChannelMessageKey();
        key.setChannelId(userMessage.getKey().getChannelId());
        key.setMessageId(userMessage.getKey().getMessageId());
        
        ChannelMessage result = ChannelMessage.builder()
                .key(key)
                .userId(userMessage.getKey().getUserId())
                .content(userMessage.getContent())
                .type(userMessage.getType())
                .timestamp(userMessage.getKey().getTimestamp())
                .build();
        
        log.info("🔍 Debug - Converted ChannelMessage: key={}, userId={}, content={}", 
                result.getKey(), result.getUserId(), result.getContent());
        
        return result;
    }

    /**
     * Get messages for multiple channels in batch
     */
    public Map<UUID, List<ChannelMessage>> getBatchChannelMessages(List<UUID> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            throw new InvalidMessageException("Channel IDs list cannot be null or empty");
        }
        
        log.info("📨 ChannelMessageService: Getting messages for {} channels in batch", channelIds.size());
        
        try {
            return channelIds.stream()
                    .collect(Collectors.toMap(
                            channelId -> channelId,
                            channelId -> {
                                try {
                                    return getAllMessagesOfChannel(channelId);
                                } catch (Exception e) {
                                    log.error("❌ ChannelMessageService: Error getting messages for channel {}: {}", channelId, e.getMessage());
                                    return List.of();
                                }
                            }
                    ));
        } catch (Exception e) {
            log.error("❌ ChannelMessageService: Error processing batch channel messages: {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to retrieve batch channel messages", e);
        }
    }
}