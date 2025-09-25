package com.sonnguyen.chatservice.events.consumer;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.sonnguyen.chatservice.events.dto.ChannelCreatedEvent;
import com.sonnguyen.chatservice.events.dto.EventWrapper;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageKey;
import com.sonnguyen.chatservice.model.ChannelMessageType;
import com.sonnguyen.chatservice.repository.ChannelMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelCreatedEventConsumer {

    private final ChannelMessageRepository channelMessageRepository;

    @KafkaListener(topics = "notifications-topic", groupId = "chat-service-group")
    public void handleChannelCreated(EventWrapper<ChannelCreatedEvent> eventWrapper) {
        try {
            if ("NEW_CHANNEL".equals(eventWrapper.getEventType())) {
                log.info("Received ChannelCreatedEvent: {}", eventWrapper);
                createChannelNoticeMessage(eventWrapper.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing ChannelCreatedEvent: {}", e.getMessage(), e);
        }
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
            
        } catch (Exception e) {
            log.error("Error creating notice message: {}", e.getMessage(), e);
        }
    }
}
