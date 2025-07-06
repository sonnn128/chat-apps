package com.sonnguyen.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.chatservice.client.ChannelServiceClient;
import com.sonnguyen.chatservice.client.UserServiceClient;
import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.dto.response.UserResponse;
import com.sonnguyen.chatservice.events.dto.NewMessageSentEvent;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageKey;
import com.sonnguyen.chatservice.model.ChannelMessageType;
import com.sonnguyen.chatservice.repository.ChannelMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelMessageService {

    private static final String NEW_MESSAGES_TOPIC = "new-messages-topic";

    private final ChannelMessageRepository channelMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final ChannelServiceClient channelServiceClient;

    public List<ChannelMessage> getAllMessagesOfChannel(UUID channelId) {
        return channelMessageRepository.findAllByKeyChannelIdOrderByKeyMessageIdAsc(channelId);
    }

    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {
        authorizeUserForChannel(request.getChannelId(), senderId);

        ChannelMessageKey key = new ChannelMessageKey();
        key.setChannelId(request.getChannelId());
        key.setMessageId(Uuids.timeBased());

        ChannelMessage messageToSave = ChannelMessage.builder()
                .key(key)
                .userId(senderId)
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : ChannelMessageType.CHAT)
                .timestamp(new Date())
                .build();
        ChannelMessage savedMessage = channelMessageRepository.save(messageToSave);
        log.info("Message saved to Cassandra with ID: {}", savedMessage.getKey().getMessageId());


        produceNewMessageEvent(savedMessage);

        return savedMessage;
    }

    private void authorizeUserForChannel(UUID channelId, UUID userId) {
        channelServiceClient.checkUserIsParticipant(channelId, userId);
        log.info("Authorization successful for user {} in channel {}", userId, channelId);
    }

    private void produceNewMessageEvent(ChannelMessage message) {
        UserResponse senderProfile = userServiceClient.getUserById(message.getUserId());

        List<UUID> recipientIds = channelServiceClient.getParticipantIdsByChannelId(message.getKey().getChannelId());

        String senderName = senderProfile != null ? (senderProfile.getFirstname() + " " + senderProfile.getLastname()) : "A user";

        NewMessageSentEvent event = NewMessageSentEvent.builder()
                .messageId(message.getKey().getMessageId())
                .channelId(message.getKey().getChannelId())
                .senderId(message.getUserId())
                .senderName(senderName)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .recipientIds(recipientIds)
                .build();

        kafkaTemplate.send(NEW_MESSAGES_TOPIC, event);
        log.info("Produced NewMessageSentEvent for message {}", message.getKey().getMessageId());
    }
}