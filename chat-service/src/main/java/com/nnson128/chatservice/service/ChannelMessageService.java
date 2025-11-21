package com.nnson128.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.EventWrapper;
import com.nnson128.chatapps_base.models.events.message.MessageEventType;
import com.nnson128.chatapps_base.models.events.message.payloads.MessageSentPayload;
import com.nnson128.chatservice.client.ChannelServiceClient;
import com.nnson128.chatservice.client.UserServiceClient;
import com.nnson128.chatservice.dto.SenderInfo;
import com.nnson128.chatservice.dto.req.SendMessageRequest;
import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.chatservice.dto.res.ChannelMessageDto;
import com.nnson128.chatservice.dto.res.ChannelMessageKeyDto;
import com.nnson128.chatservice.model.ChannelMessage;
import com.nnson128.chatservice.model.ChannelMessageKey;
import com.nnson128.chatservice.model.ChannelMessageType;
import com.nnson128.chatservice.repository.ChannelMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelMessageService {

    private final ChannelMessageRepository channelMessageRepository;
    private final ChannelServiceClient channelServiceClient;
    private final UserServiceClient userServiceClient;
    private final MessageProducerService messageProducerService;
    private final ObjectMapper objectMapper;

    public List<ChannelMessage> getAllMessagesOfChannel(UUID channelId) {
        return channelMessageRepository.findAllByKeyChannelIdOrderByKeyMessageIdAsc(channelId);
    }

    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {

        ChannelMessage messageToSave = ChannelMessage.builder()
                .key(ChannelMessageKey.builder()
                        .channelId(request.getChannelId())
                        .messageId(Uuids.timeBased())
                        .build())
                .userId(senderId)
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : ChannelMessageType.CHAT)
                .timestamp(Instant.now())
                .timestamp(Instant.now())
                .build();

        // Check if user is participant of the channel
        ApiResponse<Boolean> isParticipant = channelServiceClient.checkUserIsParticipant(request.getChannelId(), senderId);
        if (isParticipant == null || !Boolean.TRUE.equals(isParticipant.getData())) {
            throw new com.nnson128.chatapps_base.exception.CommonException("User is not a participant of this channel", org.springframework.http.HttpStatus.FORBIDDEN);
        }

        // Save to channel_message table
        ChannelMessage savedMessage = channelMessageRepository.save(messageToSave);

        // Send notification to all channel members (including sender for multi-tab scenarios)
        ApiResponse<List<UUID>> response = channelServiceClient
                .getParticipantIdsByChannelId(savedMessage.getKey().getChannelId());

        List<UUID> allRecipientIds = response.getData();

        // Get sender information for real-time display
        SenderInfo senderInfo = getSenderInfo(savedMessage.getUserId());

        // Create MessageSentPayload - include all channel members
        MessageSentPayload event = MessageSentPayload.builder()
                .eventType(MessageEventType.MESSAGE_SENT)
                .eventId(UUID.randomUUID().toString())
                .timestamp(savedMessage.getTimestamp())
                .messageId(savedMessage.getKey().getMessageId().toString())
                .channelId(savedMessage.getKey().getChannelId().toString())
                .type(savedMessage.getType() != null ? savedMessage.getType().name() : null)
                .content(savedMessage.getContent())
                .userId(savedMessage.getUserId())
                .senderName(senderInfo.name())
                .senderAvatar(senderInfo.avatar())
                .recipientIds(allRecipientIds)
                .build();

        // Wrap event
        EventWrapper<?> wrapper = EventWrapper.builder()
                .eventType("MESSAGE_SENT")
                .eventId(event.getEventId())
                .timestamp(java.time.LocalDateTime.now())
                .payload(event)
                .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, MessageEventType.MESSAGE_SENT.name(), wrapper, null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message event", e);
        }

        return savedMessage;
    }

    /**
     * Get messages for multiple channels in batch
     */
    public Map<UUID, List<ChannelMessageDto>> getBatchChannelMessages(List<UUID> channelIds) {
        if (channelIds == null) {
            return new HashMap<>();
        }

        if (channelIds.isEmpty()) {
            return new HashMap<>();
        }
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
                                return List.of();
                            }
                        }));
    }

    /**
     * Convert ChannelMessage to ChannelMessageDto
     */
    private ChannelMessageDto convertToDto(ChannelMessage message) {
        if (message == null) {
            return null;
        }

        // Get sender info
        SenderInfo senderInfo = getSenderInfo(message.getUserId());

        return ChannelMessageDto.builder()
                .key(ChannelMessageKeyDto.builder()
                        .channelId(message.getKey().getChannelId())
                        .messageId(message.getKey().getMessageId())
                        .build())
                .userId(message.getUserId())
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .senderName(senderInfo.name())
                .senderAvatar(senderInfo.avatar())
                .build();
    }

    /**
     * Get sender information for real-time display
     */
    private SenderInfo getSenderInfo(UUID userId) {
        try {
            var userResponse = userServiceClient.getUserById(userId);

            String name = userResponse.getFirstname() + " " + userResponse.getLastname();
            String avatar = userResponse.getAvatarUrl();

            return new SenderInfo(name, avatar);
        } catch (Exception e) {
            return new SenderInfo("Unknown User", null);
        }
    }
}