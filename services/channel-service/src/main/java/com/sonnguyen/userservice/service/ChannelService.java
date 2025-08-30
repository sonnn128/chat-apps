package com.sonnguyen.userservice.service;

import com.sonnguyen.userservice.client.ChatServiceClient;
import com.sonnguyen.userservice.client.UserServiceClient;
import com.sonnguyen.userservice.dto.request.CreateChannelRequest;
import com.sonnguyen.userservice.dto.request.SendMessageRequest;
import com.sonnguyen.userservice.dto.response.ChannelResponse;
import com.sonnguyen.userservice.dto.response.CreateChannelResponse;
import com.sonnguyen.userservice.dto.response.MessageResponse;
import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.events.dto.EventWrapper;
import com.sonnguyen.userservice.events.dto.NewChannelCreatedEvent;
import com.sonnguyen.userservice.exception.CommonException;
import com.sonnguyen.userservice.model.Channel;
import com.sonnguyen.userservice.model.ChannelParticipant;
import com.sonnguyen.userservice.model.ParticipantRole;
import com.sonnguyen.userservice.model.message.ChannelMessageKey;
import com.sonnguyen.userservice.model.message.ChannelMessageType;
import com.sonnguyen.userservice.repository.ChannelParticipantRepository;
import com.sonnguyen.userservice.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final ChannelRepository channelRepository;
    private final ChannelParticipantRepository participantRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;

    public CreateChannelResponse createChannel(CreateChannelRequest request, UUID creatorId) {
        Channel newChannel = Channel.builder()
                .channelName(request.getChannelName())
                .createdBy(creatorId)
                .build();
        Channel savedChannel = channelRepository.save(newChannel);

        Set<UUID> allMemberIds = new HashSet<>(request.getMemberIds());
        allMemberIds.add(creatorId);

        Set<ChannelParticipant> participants = allMemberIds.stream()
                .map(memberId -> {
                    ParticipantRole role = memberId.equals(creatorId) ? ParticipantRole.ADMIN : ParticipantRole.MEMBER;
                    return ChannelParticipant.builder()
                            .channel(savedChannel)
                            .userId(memberId)
                            .role(role)
                            .build();
                })
                .collect(Collectors.toSet());
        participantRepository.saveAll(participants);
        log.info("Channel {} created with {} participants.", savedChannel.getId(), participants.size());

        produceNewChannelEvent(savedChannel, creatorId, allMemberIds.stream()
                .filter(id -> !id.equals(creatorId))
                .toList());

        SendMessageRequest noticeRequest = SendMessageRequest.builder()
                .channelId(savedChannel.getId())
                .content("Bạn đã tạo channel " + savedChannel.getChannelName() + " thành công")
                .type(ChannelMessageType.NOTICE)
                .build();
        ResponseEntity<MessageResponse> noticeResponse =
                chatServiceClient.saveMessageOnly(noticeRequest, creatorId.toString());
        MessageResponse savedNoticeMessage = noticeResponse.getBody();
        log.info("Notice message saved for creator: {}", savedNoticeMessage);

//        return CreateChannelResponse.from(ChannelResponse.from(savedChannel), savedNoticeMessage);
        return CreateChannelResponse.from(ChannelResponse.from(savedChannel), savedNoticeMessage);
    }


    public boolean isUserParticipant(UUID channelId, UUID userId) {
        return participantRepository.existsByChannelIdAndUserId(channelId, userId);
    }

    public List<UUID> getParticipantIdsByChannelId(UUID channelId) {
        return participantRepository.findByChannelId(channelId).stream()
                .map(ChannelParticipant::getUserId)
                .collect(Collectors.toList());
    }

    private void produceNewChannelEvent(Channel channel, UUID creatorId, List<UUID> memberIds) {
        UserResponse creatorProfile = userServiceClient.getUserById(creatorId);
        String creatorName = creatorProfile != null ? (creatorProfile.getFirstname() + " " + creatorProfile.getLastname()) : "A user";

        NewChannelCreatedEvent event = NewChannelCreatedEvent.builder()
                .channelId(channel.getId())
                .channelName(channel.getChannelName())
                .creatorId(creatorId)
                .creatorName(creatorName)
                .createdAt(channel.getCreatedAt())
                .memberIds(memberIds)
                .build();
        log.warn(event.toString());
        kafkaTemplate.send(NOTIFICATION_TOPIC, new EventWrapper<>("NEW_CHANNEL", event));
        log.info("Produced NewChannelCreatedEvent for channel {}", channel.getId());

    }


    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        List<ChannelParticipant> participations = participantRepository.findByUserId(userId);

        return participations.stream()
                .map(ChannelParticipant::getChannel)
                .map(ChannelResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteChannel(UUID channelId) {
        if (channelRepository.existsById(channelId)) {
            channelRepository.deleteById(channelId);
        }
        throw new CommonException(channelId + " Not found", HttpStatus.NOT_FOUND);
    }

}