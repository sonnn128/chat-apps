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
        // BƯỚC 1: KIỂM TRA QUYỀN (Placeholder - Cần triển khai Feign Client)
        // log.info("Checking if user {} is a member of channel {}", senderId, request.getChannelId());
        // try {
        //     channelServiceClient.checkUserIsParticipant(request.getChannelId(), senderId);
        // } catch (Exception e) { // Bắt FeignException.Forbidden cụ thể sẽ tốt hơn
        //     log.warn("Authorization failed for user {} in channel {}", senderId, request.getChannelId());
        //     throw new AccessDeniedException("You are not a member of this channel.");
        // }
        // log.info("Authorization successful.");

        // BƯỚC 2: XÂY DỰNG ĐỐI TƯỢNG MESSAGE (Logic đã được sửa lại)
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

        // BƯỚC 3: LƯU VÀO DATABASE
        ChannelMessage savedMessage = channelMessageRepository.save(messageToSave);
        log.info("Message saved successfully with id: {}", savedMessage.getKey().getMessageId());

        // BƯỚC 4: ĐẨY QUA WEBSOCKET (Placeholder)
        // ... logic push WebSocket ở đây ...

        return savedMessage;
    }
}