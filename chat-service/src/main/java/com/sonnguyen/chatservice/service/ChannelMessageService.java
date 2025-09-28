package com.sonnguyen.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.chatservice.client.ChannelServiceClient;
import com.sonnguyen.chatservice.client.UserServiceClient;
import com.sonnguyen.chatservice.dto.SenderInfo;
import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.dto.response.ApiResponse;
import com.sonnguyen.chatservice.dto.response.ChannelMessageDto;
import com.sonnguyen.chatservice.dto.response.ChannelMessageKeyDto;
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
import java.util.HashMap;
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
    private final UserServiceClient userServiceClient;

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
//        create message
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
    }

    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {
        ChannelMessage savedMessage = saveMessage(request, senderId);
        // Send notification to others (exclude sender - sender gets message via HTTP response)
        produceNewMessageEvent(savedMessage, senderId);
        return savedMessage;
    }


    private void produceNewMessageEvent(ChannelMessage message, UUID excludeUserId) {
        ApiResponse<List<UUID>> response = channelServiceClient
                .getParticipantIdsByChannelId(message.getKey().getChannelId());

        if (!response.isSuccess() || response.getData() == null) {
            log.error("Failed to get participant IDs for channel {}", message.getKey().getChannelId());
            return;
        }

        List<UUID> allRecipientIds = response.getData();

        // Exclude sender from recipients (sender already received the message via HTTP response)
        List<UUID> recipientIds = allRecipientIds.stream()
                .filter(userId -> !userId.equals(excludeUserId))
                .toList();

        log.info("📨 ChannelMessageService: Sending message to {} recipients (excluded: {})",
                recipientIds.size(), excludeUserId);

        // Get sender information for real-time display
        SenderInfo senderInfo = getSenderInfo(message.getUserId());

        MessageSentEvent event = MessageSentEvent.builder()
                .key(MessageSentEventKey.builder()
                        .channelId(message.getKey().getChannelId())
                        .messageId(message.getKey().getMessageId())
                        .build())
                .type(message.getType())
                .userId(message.getUserId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .senderName(senderInfo.name())
                .senderAvatar(senderInfo.avatar())
                .recipientIds(recipientIds)
                .build();

        kafkaTemplate.send(NOTIFICATION_TOPIC, EventWrapper.builder()
                        .eventType(MessageSentEvent.EVENT_TYPE)
                        .payload(event)
                .build());
    }

    /**
     * Save message to user_message table for efficient user-based queries
     */
    private void saveToUserMessageTable(ChannelMessage message) {
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
    }

    /**
     * Get all messages sent by a specific user across all channels
     * This is optimized for initial UI rendering - no need to call channel-service
     * API
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
                            Collectors.toList()));

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
    public Map<UUID, List<ChannelMessageDto>> getBatchChannelMessages(List<UUID> channelIds) {
        if (channelIds == null) {
            log.warn("📨 ChannelMessageService: Channel IDs list is null, returning empty map");
            return new HashMap<>();
        }

        if (channelIds.isEmpty()) {
            log.info("📨 ChannelMessageService: Channel IDs list is empty, returning empty map");
            return new HashMap<>();
        }

        log.info("📨 ChannelMessageService: Getting messages for {} channels in batch", channelIds.size());

        try {
            return channelIds.stream()
                    .collect(Collectors.toMap(
                            channelId -> channelId,
                            channelId -> {
                                try {
                                    List<ChannelMessage> messages = getAllMessagesOfChannel(channelId);
                                    return messages.stream()
                                            .map(this::convertToDto)
                                            .collect(Collectors.toList());
                                } catch (Exception e) {
                                    log.error("❌ ChannelMessageService: Error getting messages for channel {}: {}",
                                            channelId, e.getMessage());
                                    return List.of();
                                }
                            }));
        } catch (Exception e) {
            log.error("❌ ChannelMessageService: Error processing batch channel messages: {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to retrieve batch channel messages", e);
        }
    }

    /**
     * Convert ChannelMessage to ChannelMessageDto
     */
    private ChannelMessageDto convertToDto(ChannelMessage message) {
        if (message == null) {
            return null;
        }
        
        return ChannelMessageDto.builder()
                .key(ChannelMessageKeyDto.builder()
                        .channelId(message.getKey().getChannelId())
                        .messageId(message.getKey().getMessageId())
                        .build())
                .userId(message.getUserId())
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }

    /**
     * Get sender information for real-time display
     */
    private SenderInfo getSenderInfo(UUID userId) {
        try {
            log.info("🔍 ChannelMessageService: Fetching user info for sender: {}", userId);
            var userResponse = userServiceClient.getUserById(userId);
            
            String name = userResponse.getFirstname() + " " + userResponse.getLastname();
            String avatar = userResponse.getAvatarUrl();
            
            log.info("✅ ChannelMessageService: Successfully fetched sender info: {} {}", 
                    userResponse.getFirstname(), userResponse.getLastname());
            
            return new SenderInfo(name, avatar);
        } catch (Exception e) {
            log.warn("❌ ChannelMessageService: Could not get sender information for user {}: {}", userId, e.getMessage());
            return new SenderInfo("Unknown User", null);
        }
    }
}