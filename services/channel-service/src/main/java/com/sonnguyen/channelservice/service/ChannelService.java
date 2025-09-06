package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.client.ChatServiceClient;
import com.sonnguyen.channelservice.client.UserServiceClient;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.CreateChannelResponse;
import com.sonnguyen.channelservice.dto.response.MessageResponse;
import com.sonnguyen.channelservice.dto.response.UserResponse;
import com.sonnguyen.channelservice.events.dto.EventWrapper;
import com.sonnguyen.channelservice.events.dto.MembersAddedToChannelEvent;
import com.sonnguyen.channelservice.events.dto.NewChannelCreatedEvent;
import com.sonnguyen.channelservice.exception.CommonException;
import com.sonnguyen.channelservice.model.Channel;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import com.sonnguyen.channelservice.model.ParticipantRole;
import com.sonnguyen.channelservice.model.message.ChannelMessageType;
import com.sonnguyen.channelservice.repository.ChannelParticipantRepository;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final ChannelParticipantRepository channelParticipantRepository;

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
                .content("Kênh " + savedChannel.getChannelName() + " đã được tạo thành công")
                .type(ChannelMessageType.NOTICE)
                .build();
        ResponseEntity<MessageResponse> noticeResponse =
                chatServiceClient.saveMessageOnly(noticeRequest, creatorId.toString());
        MessageResponse savedNoticeMessage = noticeResponse.getBody();
        log.info("Notice message saved for creator: {}", savedNoticeMessage);

//        return CreateChannelResponse.from(ChannelResponse.from(savedChannel), savedNoticeMessage);
        return CreateChannelResponse.from(ChannelResponse.from(savedChannel), savedNoticeMessage,participants);
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

    public UUID deleteChannel(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new CommonException(channelId + " Not found", HttpStatus.NOT_FOUND);
        }
        channelRepository.deleteById(channelId);
        return channelId;
    }

    public boolean leaveChannel(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        Optional<ChannelParticipant> participantOpt =
                channelParticipantRepository.findByChannelIdAndUserId(channelId, userId);

        if (participantOpt.isEmpty()) {
            log.warn("User {} không phải là member của channel {}", userId, channelId);
            return false;
        }
        UserResponse user = userServiceClient.getUserById(userId);
        String noticeContent = String.format("Người dùng %s đã rời khỏi nhóm", user.getFirstname() + " " + user.getLastname());

        SendMessageRequest noticeRequest = SendMessageRequest.builder()
                .channelId(channelId)
                .content(noticeContent)
                .type(ChannelMessageType.NOTICE)
                .build();

        log.info(">>> Start calling saveMessageOnly");
        ResponseEntity<MessageResponse> noticeResponse =
                chatServiceClient.saveMessageOnly(noticeRequest, userId.toString());
        log.info(">>> saveMessageOnly returned: {}", noticeResponse);
        MessageResponse savedNoticeMessage = noticeResponse.getBody();
        log.info("Notice message saved for creator: {}", savedNoticeMessage);

        channelParticipantRepository.delete(participantOpt.get());




        return true;
    }


    public List<ChannelParticipant> addMemberToChannel(UUID channelId, List<UUID> userIds, UUID addByUserId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        UserResponse addedByUser = userServiceClient.getUserById(addByUserId);
        log.info("AddBy User: {}", addedByUser);

        List<ChannelParticipant> newMembers = new ArrayList<>();
        List<String> addedUserNames = new ArrayList<>();

        for (UUID userId : userIds) {
            boolean alreadyMember = channelParticipantRepository.existsByChannelIdAndUserId(channelId, userId);
            if (!alreadyMember) {
                ChannelParticipant participant = ChannelParticipant.builder()
                        .channel(channel)
                        .userId(userId)
                        .role(ParticipantRole.MEMBER)
                        .build();

                newMembers.add(channelParticipantRepository.save(participant));

                UserResponse addedUser = userServiceClient.getUserById(userId);
                addedUserNames.add(addedUser.getFirstname() + " " + addedUser.getLastname());
            }
        }
//            boolean leaveChannel(UUID userId, UUID channelId);

        // Tạo nội dung thông báo
        String addedByName = addedByUser.getFirstname() + " " + addedByUser.getLastname();
        String noticeContent;

        if (addedUserNames.size() == 1) {
            noticeContent = String.format("%s đã thêm %s vào nhóm", addedByName, addedUserNames.get(0));
        } else if (!addedUserNames.isEmpty()) {
            noticeContent = String.format("%s đã thêm %s vào nhóm", addedByName, String.join(", ", addedUserNames));
        } else {
            noticeContent = null;
        }

        log.info("Added User: {}", noticeContent);
        if (noticeContent != null) {
            SendMessageRequest noticeRequest = SendMessageRequest.builder()
                    .channelId(channelId)
                    .content(noticeContent)
                    .type(ChannelMessageType.NOTICE)
                    .build();
            ResponseEntity<MessageResponse> noticeResponse =
                    chatServiceClient.saveMessageOnly(noticeRequest, addByUserId.toString());
            MessageResponse savedNoticeMessage = noticeResponse.getBody();
            log.info("Notice message saved for creator: {}", savedNoticeMessage);

        }

        return newMembers;
    }
    private void produceAddMemberEvent(UUID channelId, UUID addedByUserId, List<UUID> newMemberIds) {
        // Lấy profile của người thêm
        UserResponse addedByProfile = userServiceClient.getUserById(addedByUserId);
        String addedByName = addedByProfile != null
                ? (addedByProfile.getFirstname() + " " + addedByProfile.getLastname())
                : "A user";

        // Tạo event
        MembersAddedToChannelEvent event = MembersAddedToChannelEvent.builder()
                .channelId(channelId)
                .addedByUserId(addedByUserId)
                .addedByName(addedByName)
                .newMemberIds(newMemberIds)
                .addedAt(Instant.now())
                .build();

        // Gửi Kafka
        kafkaTemplate.send(NOTIFICATION_TOPIC, new EventWrapper<>("MEMBERS_ADDED", event));
        log.info("Produced MembersAddedToChannelEvent for channel {} by {}", channelId, addedByUserId);
    }
}
