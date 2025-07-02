package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.model.Channel;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import com.sonnguyen.channelservice.model.ParticipantRole;
import com.sonnguyen.channelservice.repository.ChannelParticipantRepository;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelParticipantRepository participantRepository;

    @Transactional // Đảm bảo tất cả các thao tác DB được thực hiện thành công hoặc rollback
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

        return ChannelResponse.builder()
                .id(savedChannel.getId())
                .channelName(savedChannel.getChannelName())
                .createdBy(savedChannel.getCreatedBy())
                .createdAt(savedChannel.getCreatedAt())
                .build();
    }

}
