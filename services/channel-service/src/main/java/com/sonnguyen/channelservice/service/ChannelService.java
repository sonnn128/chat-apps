package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.model.Channel;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import com.sonnguyen.channelservice.model.ParticipantRole;
import com.sonnguyen.channelservice.repository.ChannelParticipantRepository;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelParticipantRepository participantRepository;

    @Transactional
    public ChannelResponse createChannel(CreateChannelRequest request, UUID creatorId) {
        log.info("Creating new channel with name '{}' by user {}", request.getChannelName(), creatorId);

        // 1. Tạo và lưu Channel entity
        Channel newChannel = Channel.builder()
                .channelName(request.getChannelName())
                .createdBy(creatorId)
                .build();
        Channel savedChannel = channelRepository.save(newChannel);
        log.info("Channel created with ID: {}", savedChannel.getId());

        // 2. Chuẩn bị danh sách thành viên (bao gồm cả người tạo)
        Set<UUID> allMemberIds = new HashSet<>(request.getMemberIds());
        allMemberIds.add(creatorId);

        // 3. Tạo các bản ghi ChannelParticipant
        Set<ChannelParticipant> participants = allMemberIds.stream()
                .map(memberId -> {
                    // Người tạo kênh sẽ có vai trò là ADMIN
                    ParticipantRole role = memberId.equals(creatorId) ? ParticipantRole.ADMIN : ParticipantRole.MEMBER;
                    return ChannelParticipant.builder()
                            .channel(savedChannel)
                            .userId(memberId)
                            .role(role)
                            .build();
                })
                .collect(Collectors.toSet());

        participantRepository.saveAll(participants);
        log.info("Added {} participants to channel {}", participants.size(), savedChannel.getId());

        // 4. Chuyển đổi entity thành DTO để trả về
        return ChannelResponse.builder()
                .id(savedChannel.getId())
                .channelName(savedChannel.getChannelName())
                .createdBy(savedChannel.getCreatedBy())
                .createdAt(savedChannel.getCreatedAt())
                .build();
    }

    /**
     * Kiểm tra xem một user có phải là thành viên của một kênh hay không.
     * API này sẽ được chat-service gọi để kiểm tra quyền.
     */
    public boolean isUserParticipant(UUID channelId, UUID userId) {
        return participantRepository.existsByChannelIdAndUserId(channelId, userId);
    }

    /**
     * Lấy danh sách các kênh mà một người dùng tham gia.
     */
    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        List<ChannelParticipant> participations = participantRepository.findByUserId(userId);
        return participations.stream()
                .map(p -> p.getChannel()) // Lấy đối tượng Channel từ mối quan hệ
                .map(channel -> ChannelResponse.builder() // Chuyển đổi sang DTO
                        .id(channel.getId())
                        .channelName(channel.getChannelName())
                        .createdBy(channel.getCreatedBy())
                        .createdAt(channel.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
