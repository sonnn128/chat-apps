package com.sonnguyen.chatservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageKey;
import com.sonnguyen.chatservice.model.ChannelMessageType;
import com.sonnguyen.chatservice.repository.ChannelMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelMessageService {

    private final ChannelMessageRepository channelMessageRepository;
    // Inject Feign Client để giao tiếp với channel-service
    // private final ChannelServiceClient channelServiceClient;

    public List<ChannelMessage> getAllMessagesOfChannel(UUID channelId) {
        return channelMessageRepository.findAllByKeyChannelIdOrderByKeyMessageIdAsc(channelId);
    }

    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {
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
        log.info("Message saved successfully with id: {}", savedMessage.getKey().getMessageId());

        return savedMessage;
    }
}