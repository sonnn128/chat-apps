package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.client.UserServiceClient;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.UserResponse;
import com.sonnguyen.channelservice.events.dto.NewChannelCreatedEvent;
import com.sonnguyen.channelservice.model.Channel;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import com.sonnguyen.channelservice.model.ParticipantRole;
import com.sonnguyen.channelservice.repository.ChannelParticipantRepository;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final String NEW_CHANNELS_TOPIC = "new-channels-topic";

    private final ChannelRepository channelRepository;
    private final ChannelParticipantRepository participantRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;

    @Transactional
    public ChannelResponse createChannel(CreateChannelRequest request, UUID creatorId) {
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

        produceNewChannelEvent(savedChannel, creatorId, new ArrayList<>(allMemberIds));

        return toChannelResponse(savedChannel);
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

        kafkaTemplate.send(NEW_CHANNELS_TOPIC, event);
        log.info("Produced NewChannelCreatedEvent for channel {}", channel.getId());

    }

    private ChannelResponse toChannelResponse(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .createdBy(channel.getCreatedBy())
                .createdAt(channel.getCreatedAt())
                .build();
    }

    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        List<ChannelParticipant> participations = participantRepository.findByUserId(userId);

        return participations.stream()
                .map(ChannelParticipant::getChannel)
                .map(this::toChannelResponse)
                .collect(Collectors.toList());
    }

}