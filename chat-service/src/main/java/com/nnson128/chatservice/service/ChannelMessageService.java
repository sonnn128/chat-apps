package com.nnson128.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.exception.CommonException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ChannelMessageService {

    private final ChannelMessageRepository channelMessageRepository;
    private final ChannelServiceClient channelServiceClient;
    private final UserServiceClient userServiceClient;
    private final MessageProducerService messageProducerService;

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
            throw new CommonException("User is not a participant of this channel", HttpStatus.FORBIDDEN);
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

    public void deleteMessage(UUID channelId, UUID messageId, UUID userId) {
        // 1. Find the message
        ChannelMessage message = channelMessageRepository.findById(
                ChannelMessageKey.builder()
                    .channelId(channelId)
                    .messageId(messageId)
                    .build())
            .orElseThrow(() -> new CommonException("Message not found", HttpStatus.NOT_FOUND));

        // 2. Validate ownership
        if (!message.getUserId().equals(userId)) {
            throw new CommonException("You can only delete your own messages", HttpStatus.FORBIDDEN);
        }

        // 3. Update message status
        message.setType(ChannelMessageType.DELETED);
        message.setContent(""); // Optional: clear content or keep it for audit? Clearing it for privacy.

        ChannelMessage savedMessage = channelMessageRepository.save(message);

        // 4. Send notification (Event Sourcing)
        // Re-fetch participants to broadcast the update
        ApiResponse<List<UUID>> response = channelServiceClient
            .getParticipantIdsByChannelId(channelId);

        List<UUID> allRecipientIds = response.getData();
        SenderInfo senderInfo = getSenderInfo(userId);

        MessageSentPayload event = MessageSentPayload.builder()
            .eventType(MessageEventType.MESSAGE_SENT) // Reusing SENT event, frontend filters by type='DELETED' is one way, or use MESSAGE_UPDATED if available
            .eventId(UUID.randomUUID().toString())
            .timestamp(message.getTimestamp())
            .messageId(messageId.toString())
            .channelId(channelId.toString())
            .type("DELETED") // Explicitly set type to DELETED
            .content("")
            .userId(userId)
            .senderName(senderInfo.name())
            .senderAvatar(senderInfo.avatar())
            .recipientIds(allRecipientIds)
            .build();

        EventWrapper<?> wrapper = EventWrapper.builder()
            .eventType("MESSAGE_SENT")
            .eventId(event.getEventId())
            .timestamp(java.time.LocalDateTime.now())
            .payload(event)
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, MessageEventType.MESSAGE_SENT.name(), wrapper, null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to broadcast delete event", e);
        }
    }

    /**
     * Get messages for multiple channels in batch
     */
    public Map<UUID, List<ChannelMessageDto>> getBatchChannelMessages(List<UUID> channelIds) {
        return channelIds.stream()
            .collect(Collectors.toMap(channelId -> channelId, channelId -> {
                // Get latest 30 messages (Newest First because of DESC clustering order)
                List<ChannelMessage> messages = new ArrayList<>(
                    channelMessageRepository.findByKeyChannelId(channelId, PageRequest.of(0, 30)).getContent()
                );

                // Reverse to get Oldest First (Chronological order)
                Collections.reverse(messages);

                return messages.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
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
