package com.sonnguyen.channelservice.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.model.ChannelMessage;
import com.sonnguyen.channelservice.model.ChannelMessageKey;
import com.sonnguyen.channelservice.model.ChannelMessageType;
import com.sonnguyen.channelservice.repository.ChannelMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelMessageService {
    private final ChannelMessageRepository channelMessageRepository;

    public List<ChannelMessage> getAllMessagesOfChannel(UUID channelId) {
        return channelMessageRepository.findAllByKeyChannelIdOrderByKeyMessageIdAsc(channelId);
    }
    public ChannelMessage sendMessage(SendMessageRequest request, UUID senderId) {
        ChannelMessage messageToSave = request.getChannelMessage();
        ChannelMessageKey key = new ChannelMessageKey();
        key.setChannelId(request.getChannelId());
        key.setMessageId(Uuids.timeBased()); // Tạo message_id dựa trên thời gian

        messageToSave.setKey(key);
        messageToSave.setUserId(senderId); // ID người gửi
        messageToSave.setTimestamp(new Date()); // Thời gian hiện tại

        if (messageToSave.getType() == null) {
            messageToSave.setType(ChannelMessageType.CHAT);
        }

        return channelMessageRepository.save(messageToSave);
    }
}