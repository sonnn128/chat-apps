package com.nnson128.relationshipservice.service;

import com.nnson128.relationshipservice.client.UserServiceClient;
import com.nnson128.relationshipservice.client.ChatServiceClient;
import com.nnson128.relationshipservice.dto.request.CreateChannelRequest;
import com.nnson128.relationshipservice.dto.request.SendMessageRequest;
import com.nnson128.relationshipservice.dto.message.ChannelMessageDto;
import com.nnson128.relationshipservice.dto.response.ChannelResponse;
import com.nnson128.chatapps_base.dto.res.UserResponse;
import com.nnson128.relationshipservice.dto.response.ChannelParticipantResponse;
import com.nnson128.relationshipservice.dto.response.AddPeopleResponse;
import com.nnson128.relationshipservice.exception.ChannelNotFoundException;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelCreatedPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelUpdatedPayload;
import com.nnson128.chatapps_base.models.events.channel.payloads.MembersAddedPayload;
import com.nnson128.relationshipservice.model.channel.Channel;
import com.nnson128.relationshipservice.model.membership.Membership;
import com.nnson128.relationshipservice.dto.message.ChannelMessageType;
import com.nnson128.relationshipservice.model.membership.MembershipKey;
import com.nnson128.relationshipservice.model.membership.MembershipRole;
import com.nnson128.relationshipservice.repository.ChannelRepository;
import com.nnson128.relationshipservice.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageProducerService messageProducerService;
    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;
    private final MembershipRepository membershipRepository;

    public ChannelResponse createChannel(CreateChannelRequest request, UUID creatorId) {
        // 1. create channel
        Channel newChannel = Channel.builder()
            .channelName(request.getChannelName())
            .channelType(Channel.GROUP)
            .build();

        // 2. save and get channel
        Channel savedChannel = channelRepository.save(newChannel);

        // 3. create relationship
        membershipRepository.save(Membership.builder()
            .membershipKey(MembershipKey.builder()
                .channelId(savedChannel.getId())
                .userId(creatorId)
                .build())
            .role(MembershipRole.ADMIN)
            .joinedAt(LocalDateTime.now())
            .build());

        // 4. create notice message for immediate response to sender
        ChannelMessageDto noticeMessage = createNoticeMessage(savedChannel, creatorId);

        // 4.5. save notice message to database immediately (synchronous) so it appears on refresh
        try {
            SendMessageRequest noticeRequest = SendMessageRequest.builder()
                .channelId(savedChannel.getId())
                .content(noticeMessage.getContent())
                .type(ChannelMessageType.NOTICE)
                .build();
            chatServiceClient.sendMessage(creatorId, noticeRequest);
        } catch (Exception e) {
        }

        // 5. publish event for chat-service to save message and notification service to notify others (exclude sender)
        List<UUID> memberIds = List.of(creatorId);
        produceNewChannelEvent(savedChannel, creatorId, memberIds);

        // 6. Get detailed user info for the creator
        List<ChannelParticipantResponse> participants = getChannelParticipants(savedChannel.getId(), memberIds);

        // 7. return response with notice message (sender gets message immediately via HTTP)
        return ChannelResponse.builder()
            .id(savedChannel.getId())
            .channelName(savedChannel.getChannelName())
            .avatar(savedChannel.getAvatar())
            .createdAt(savedChannel.getCreatedAt())
            .message(noticeMessage)
            .memberIds(memberIds)
            .participants(participants)
            .build();
    }

    private ChannelMessageDto createNoticeMessage(Channel channel, UUID creatorId) {
        // Generate message ID
        UUID messageId = UUID.randomUUID();

        // Get creator name
        String creatorName = "A user";
        UserResponse creatorResponse = userServiceClient.getUserById(creatorId);
        if (creatorResponse != null) {
            String firstName = creatorResponse.getFirstname();
            String lastName = creatorResponse.getLastname();
            if (firstName != null && lastName != null) {
                creatorName = firstName + " " + lastName;
            }
        }

        // Create notice message content
        String channelName = channel.getChannelName() != null ? channel.getChannelName() : "kênh";
        String content = "Kênh " + channelName + " đã được tạo thành công";

        // Create notice message

        return ChannelMessageDto.createNoticeMessage(
            channel.getId(),
            messageId,
            creatorId,
            content
        );
    }

    private ChannelMessageDto createAddPeopleNoticeMessage(Channel channel, UUID addedByUserId, List<ChannelParticipantResponse> newMembers) {
        // Generate message ID
        UUID messageId = UUID.randomUUID();

        // Get added by user name
        String addedByUserName = "A user";
        UserResponse addedByResponse = userServiceClient.getUserById(addedByUserId);
        if (addedByResponse != null) {
            String firstName = addedByResponse.getFirstname();
            String lastName = addedByResponse.getLastname();
            if (firstName != null && lastName != null) {
                addedByUserName = firstName + " " + lastName;
            }
        }

        // Create notice message content
        String memberNames = newMembers.stream()
            .map(member -> member.getFirstname() + " " + member.getLastname())
            .collect(java.util.stream.Collectors.joining(", "));

        String content = addedByUserName + " đã thêm " + memberNames + " vào kênh";

        // Create notice message
        ChannelMessageDto noticeMessage = ChannelMessageDto.createNoticeMessage(
            channel.getId(),
            messageId,
            addedByUserId,
            content
        );

        return noticeMessage;
    }

    private void produceAddPeopleEvent(Channel channel, UUID addedByUserId, List<UUID> newMemberIds, List<ChannelParticipantResponse> newMembers) {
        // Get added by user name
        String addedByUserName = "A user";
        try {
            UserResponse addedByResponse = userServiceClient.getUserById(addedByUserId);
            if (addedByResponse != null) {
                String firstName = addedByResponse.getFirstname();
                String lastName = addedByResponse.getLastname();
                if (firstName != null && lastName != null) {
                    addedByUserName = firstName + " " + lastName;
                }
            }
        } catch (Exception e) {
        }

        // Get all member IDs for the channel
        List<UUID> allMemberIds = getParticipantIdsByChannelId(channel.getId());

        // Create MembersAddedPayload
        MembersAddedPayload event = MembersAddedPayload.builder()
            .channelId(channel.getId())
            .channelName(channel.getChannelName())
            .addedByUserId(addedByUserId)
            .addedByUserName(addedByUserName)
            .newMemberIds(newMemberIds)
            .allMemberIds(allMemberIds)
            .build();

        // Publish event
        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, event);
        } catch (Exception e) {
            // Log error
        }
    }

    private void produceNewChannelEvent(Channel channel, UUID creatorId, List<UUID> memberIds) {
        UserResponse creatorResponse = userServiceClient.getUserById(creatorId);
        String creatorName = "A user";
        if (creatorResponse != null) {
            String firstName = creatorResponse.getFirstname();
            String lastName = creatorResponse.getLastname();
            if (firstName != null && lastName != null) {
                creatorName = firstName + " " + lastName;
            }
        }

        ChannelCreatedPayload event = ChannelCreatedPayload.builder()
            .channelId(channel.getId())
            .channelName(channel.getChannelName())
            .creatorId(creatorId)
            .creatorName(creatorName)
            .createdAt(channel.getCreatedAt())
            .memberIds(memberIds)
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, event);
        } catch (Exception e) {
            // Log error
        }
    }


    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);

        return memberships.stream()
            .map(membership -> {
                UUID channelId = membership.getMembershipKey().getChannelId();
                return channelRepository.findById(channelId)
                    .map(channel -> {
                        ChannelResponse response = ChannelResponse.from(channel);
                        response.setRole(membership.getRole());
                        return response;
                    })
                    .orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    //
    public List<ChannelResponse> getChannelsWithMessagesForUser(UUID userId) {
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);

        // Get all channel IDs
        List<UUID> channelIds = memberships.stream()
            .map(membership -> membership.getMembershipKey().getChannelId())
            .collect(Collectors.toList());

        // Map channelId to role
        Map<UUID, MembershipRole> channelRoles = memberships.stream()
            .collect(Collectors.toMap(
                m -> m.getMembershipKey().getChannelId(),
                Membership::getRole
            ));

        // Get all channels
        List<Channel> channels = channelRepository.findAllById(channelIds);

        // Get all messages for all channels in one API call
        Map<UUID, List<ChannelMessageDto>> rawMessagesMap = chatServiceClient.getBatchChannelMessages(channelIds);

        // Use ChannelMessageDto objects directly

        // Get all member IDs for all channels
        Map<UUID, List<UUID>> memberIdsMap = getMemberIdsForAllChannels(channelIds);

        // Build response using stream map
        // Get detailed user info for participants

        return channels.stream()
            .map(channel -> {
                List<ChannelMessageDto> channelMessages = rawMessagesMap.getOrDefault(channel.getId(), List.of());
                List<UUID> channelMembers = memberIdsMap.getOrDefault(channel.getId(), List.of());

                List<ChannelParticipantResponse> participants = getChannelParticipants(channel.getId(), channelMembers);

                return ChannelResponse.builder()
                    .id(channel.getId())
                    .channelName(channel.getChannelName())
                    .avatar(channel.getAvatar())
                    .createdAt(channel.getCreatedAt())
                    .messages(channelMessages)
                    .memberIds(channelMembers)
                    .participants(participants)
                    .role(channelRoles.get(channel.getId()))
                    .channelType(channel.getChannelType())
                    .build();
            })
            .collect(Collectors.toList());
    }

    public void deleteChannel(UUID channelId, UUID requesterId) {
        // 1. Check if channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException("Channel not found with id: " + channelId);
        }

        // 2. Check if requester is ADMIN
        Membership membership = membershipRepository.findByMembershipKeyChannelIdAndMembershipKeyUserId(channelId, requesterId)
            .orElseThrow(() -> new RuntimeException("User is not a member of this channel"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can delete the channel");
        }

        // 3. Delete all memberships
        List<Membership> memberships = membershipRepository.findByMembershipKeyChannelId(channelId);
        membershipRepository.deleteAll(memberships);

        // 4. Delete channel
        channelRepository.deleteById(channelId);

        // 5. Publish event
        // TODO: Publish CHANNEL_DELETED event
    }

    public boolean isUserParticipant(UUID channelId, UUID userId) {
        // Check if channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException("Channel with id " + channelId + " not found");
        }

        return membershipRepository.existsByMembershipKeyChannelIdAndMembershipKeyUserId(channelId, userId);
    }

    public List<UUID> getParticipantIdsByChannelId(UUID channelId) {
        // Check if channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException("Channel with id " + channelId + " not found");
        }

        return membershipRepository.findByMembershipKeyChannelId(channelId).stream()
            .map(membership -> membership.getMembershipKey().getUserId())
            .collect(Collectors.toList());
    }


    /**
     * Get member IDs for all channels
     */
    private Map<UUID, List<UUID>> getMemberIdsForAllChannels(List<UUID> channelIds) {
        return channelIds.stream()
            .collect(Collectors.toMap(
                channelId -> channelId,
                channelId -> {
                    try {
                        return membershipRepository.findByMembershipKeyChannelId(channelId)
                            .stream()
                            .map(membership -> membership.getMembershipKey().getUserId())
                            .collect(Collectors.toList());
                    } catch (Exception e) {
                        return List.<UUID>of();
                    }
                }
            ));
    }

    /**
     * Get channel IDs for a user (without messages)
     * Used by chat-service to get all channels for batch message retrieval
     */
    public List<UUID> getChannelIdsByUserId(UUID userId) {

        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);

        return memberships.stream()
            .map(membership -> membership.getMembershipKey().getChannelId())
            .collect(Collectors.toList());
    }


    public AddPeopleResponse addPeopleToChannel(UUID channelId, UUID addedByUserId, List<UUID> memberIds) {

        // Check if channel exists
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));

        // Check if user is a member of the channel
        if (!isUserParticipant(channelId, addedByUserId)) {
            throw new RuntimeException("User is not a member of this channel");
        }

        // Get current members to avoid duplicates
        List<UUID> currentMemberIds = getParticipantIdsByChannelId(channelId);

        // Filter out users who are already members
        List<UUID> newMemberIds = memberIds.stream()
            .filter(memberId -> !currentMemberIds.contains(memberId))
            .collect(java.util.stream.Collectors.toList());

        if (newMemberIds.isEmpty()) {
            return AddPeopleResponse.builder()
                .channelId(channelId)
                .channelName(channel.getChannelName())
                .newMembers(List.of())
                .message(null)
                .build();
        }

        // Add new memberships
        List<Membership> newMemberships = newMemberIds.stream()
            .map(memberId -> {
                MembershipKey key = MembershipKey.builder()
                    .channelId(channelId)
                    .userId(memberId)
                    .build();

                return Membership.builder()
                    .membershipKey(key)
                    .role(MembershipRole.MEMBER) // Set default role
                    .joinedAt(java.time.LocalDateTime.now())
                    .build();
            })
            .collect(java.util.stream.Collectors.toList());

        membershipRepository.saveAll(newMemberships);

        // Get detailed user info for new members
        List<ChannelParticipantResponse> newMembers = getChannelParticipants(channelId, newMemberIds);

        // Send notification message and real-time event
        ChannelMessageDto noticeMessage = sendMembersAddedNotification(channelId, addedByUserId, newMemberIds, channel.getChannelName());

        // Fallback if noticeMessage is null (e.g. service down)
        if (noticeMessage == null) {
            noticeMessage = createAddPeopleNoticeMessage(channel, addedByUserId, newMembers);
        }

        // Publish event for chat service to save notice message
        produceAddPeopleEvent(channel, addedByUserId, newMemberIds, newMembers);

        return AddPeopleResponse.builder()
            .channelId(channelId)
            .channelName(channel.getChannelName())
            .newMembers(newMembers)
            .message(noticeMessage)
            .build();
    }

    private ChannelMessageDto sendMembersAddedNotification(UUID channelId, UUID addedByUserId, List<UUID> newMemberIds, String channelName) {
        try {
            // Get user info for the person who added members
            UserResponse addedByResponse = userServiceClient.getUserById(addedByUserId);
            String addedByUserName = "A user";
            if (addedByResponse != null) {
                String firstName = addedByResponse.getFirstname();
                String lastName = addedByResponse.getLastname();
                if (firstName != null && lastName != null) {
                    addedByUserName = firstName + " " + lastName;
                }
            }

            // Get user info for the new members
            List<String> newMemberNames = newMemberIds.stream()
                .map(memberId -> {
                    try {
                        UserResponse userResponse = userServiceClient.getUserById(memberId);
                        if (userResponse != null) {
                            String firstName = userResponse.getFirstname();
                            String lastName = userResponse.getLastname();
                            if (firstName != null && lastName != null) {
                                return firstName + " " + lastName;
                            }
                        }
                        return "Unknown User";
                    } catch (Exception e) {
                        return "Unknown User";
                    }
                })
                .collect(java.util.stream.Collectors.toList());

            // Create notification message
            String notificationMessage = String.format("%s đã thêm %s vào kênh",
                addedByUserName,
                String.join(", ", newMemberNames));

            // Send message to chat service
            SendMessageRequest messageRequest = SendMessageRequest.builder()
                .channelId(channelId)
                .content(notificationMessage)
                .type(ChannelMessageType.NOTICE)
                .build();

            try {
                return chatServiceClient.sendMessage(addedByUserId, messageRequest);
            } catch (Exception e) {
                return null;
            }

            // Event is already sent by produceAddPeopleEvent()

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get detailed user information for a list of user IDs with roles for a specific channel
     */
    private List<ChannelParticipantResponse> getChannelParticipants(UUID channelId, List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        try {
            // Get memberships for roles
            List<Membership> memberships = membershipRepository.findByMembershipKeyChannelId(channelId);
            Map<UUID, MembershipRole> roleMap = memberships.stream()
                .collect(Collectors.toMap(
                    m -> m.getMembershipKey().getUserId(),
                    Membership::getRole
                ));

            // Call user-service to get detailed user info
            List<ChannelParticipantResponse> participants = new ArrayList<>();
            for (UUID userId : userIds) {
                try {
                    UserResponse userResponse = userServiceClient.getUserById(userId);
                    if (userResponse != null) {
                        participants.add(ChannelParticipantResponse.builder()
                            .userId(userResponse.getId())
                            .firstname(userResponse.getFirstname())
                            .lastname(userResponse.getLastname())
                            .email(userResponse.getEmail())
                            .avatarUrl(userResponse.getAvatarUrl())
                            .role(roleMap.getOrDefault(userId, MembershipRole.MEMBER))
                            .build());
                    }
                } catch (Exception e) {
                    // Create a fallback user response
                    participants.add(ChannelParticipantResponse.builder()
                        .userId(userId)
                        .firstname("User")
                        .lastname(userId.toString().substring(0, 8))
                        .email(userId.toString().substring(0, 8) + "@example.com")
                        .avatarUrl(null)
                        .role(roleMap.getOrDefault(userId, MembershipRole.MEMBER))
                        .build());
                }
            }
            return participants;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public ChannelResponse findOrCreateDirectChannel(UUID user1, UUID user2) {
        // Sort UUIDs to ensure consistent channel identification (prevent duplicate channels)
        UUID sortedUser1 = user1.compareTo(user2) < 0 ? user1 : user2;
        UUID sortedUser2 = user1.compareTo(user2) < 0 ? user2 : user1;

        // 1. Check if direct channel exists
        List<UUID> existingChannelIds = membershipRepository.findDirectChannelIds(sortedUser1, sortedUser2);
        if (!existingChannelIds.isEmpty()) {
            UUID channelId = existingChannelIds.get(0);
            Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));

            // Get messages and members
            List<ChannelMessageDto> messages = chatServiceClient.getChannelMessages(channelId);
            List<UUID> memberIds = List.of(sortedUser1, sortedUser2);
            List<ChannelParticipantResponse> participants = getChannelParticipants(channelId, memberIds);

            return ChannelResponse.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .avatar(channel.getAvatar())
                .createdAt(channel.getCreatedAt())
                .messages(messages)
                .memberIds(memberIds)
                .participants(participants)
                .channelType(channel.getChannelType())
                .build();
        }

        // 2. Create new channel
        Channel newChannel = Channel.builder()
            .channelName(null) // Direct chat usually doesn't have a name, or we can set it dynamically
            .channelType(Channel.DIRECT_MESSAGE)
            .build();
        Channel savedChannel = channelRepository.save(newChannel);

        // 3. Create memberships with sorted user IDs
        Membership membership1 = Membership.builder()
            .membershipKey(MembershipKey.builder().channelId(savedChannel.getId()).userId(sortedUser1).build())
            .role(MembershipRole.MEMBER)
            .build();

        Membership membership2 = Membership.builder()
            .membershipKey(MembershipKey.builder().channelId(savedChannel.getId()).userId(sortedUser2).build())
            .role(MembershipRole.MEMBER)
            .build();

        membershipRepository.saveAll(List.of(membership1, membership2));

        // 4. Return response
        List<UUID> memberIds = List.of(sortedUser1, sortedUser2);
        List<ChannelParticipantResponse> participants = getChannelParticipants(savedChannel.getId(), memberIds);

        return ChannelResponse.builder()
            .id(savedChannel.getId())
            .channelName(null)
            .avatar(savedChannel.getAvatar())
            .createdAt(savedChannel.getCreatedAt())
            .messages(new ArrayList<>())
            .memberIds(memberIds)
            .participants(participants)
            .channelType(savedChannel.getChannelType())
            .build();
    }

    public ChannelResponse updateChannelAvatar(UUID channelId, String avatarUrl, UUID requesterId) {
        // 1. Check if channel exists
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));

        // 2. Check if requester is a participant
        if (!isUserParticipant(channelId, requesterId)) {
            throw new RuntimeException("User is not a member of this channel");
        }

        // 3. Update avatar
        channel.setAvatar(avatarUrl);
        Channel savedChannel = channelRepository.save(channel);

        // 4. Return response
        // We need to reconstruct the full response with participants etc.
        List<UUID> memberIds = getParticipantIdsByChannelId(channelId);
        List<ChannelParticipantResponse> participants = getChannelParticipants(channelId, memberIds);

        // Get messages (optional, maybe not needed for just avatar update, but consistent with other methods)
        // For efficiency, we might skip messages here if the frontend doesn't need them immediately
        // But to be safe and consistent:
        List<ChannelMessageDto> messages = chatServiceClient.getChannelMessages(channelId);

        return ChannelResponse.builder()
            .id(savedChannel.getId())
            .channelName(savedChannel.getChannelName())
            .avatar(savedChannel.getAvatar())
            .createdAt(savedChannel.getCreatedAt())
            .messages(messages)
            .memberIds(memberIds)
            .participants(participants)
            .build();
    }

    public ChannelResponse updateChannelName(UUID channelId, String newName, UUID requesterId) {
        // 1. Check if channel exists
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));

        // 2. Check if requester is a participant (and maybe ADMIN? For now just participant)
        if (!isUserParticipant(channelId, requesterId)) {
            throw new RuntimeException("User is not a member of this channel");
        }

        // 3. Update name
        String oldName = channel.getChannelName();
        channel.setChannelName(newName);
        Channel savedChannel = channelRepository.save(channel);

        // 4. Send notification if name changed
        if (!Objects.equals(oldName, newName)) {
            sendChannelNameChangedNotification(channelId, requesterId, oldName, newName);
            produceChannelUpdatedEvent(savedChannel, requesterId);
        }

        // 5. Return response
        List<UUID> memberIds = getParticipantIdsByChannelId(channelId);
        List<ChannelParticipantResponse> participants = getChannelParticipants(channelId, memberIds);

        // Optimize: skip messages for now as frontend likely just updates header/sidebar
        // List<ChannelMessageDto> messages = chatServiceClient.getChannelMessages(channelId);

        return ChannelResponse.builder()
            .id(savedChannel.getId())
            .channelName(savedChannel.getChannelName())
            .avatar(savedChannel.getAvatar())
            .createdAt(savedChannel.getCreatedAt())
            .messages(new ArrayList<>()) // Empty messages
            .memberIds(memberIds)
            .participants(participants)
            .build();
    }

    private void sendChannelNameChangedNotification(UUID channelId, UUID requesterId, String oldName, String newName) {
        try {
            // Get user info
            UserResponse requester = userServiceClient.getUserById(requesterId);
            String requesterName = "A user";
            if (requester != null) {
                requesterName = requester.getFirstname() + " " + requester.getLastname();
            }

            String content = String.format("%s đã đổi tên đoạn chat thành \"%s\"", requesterName, newName);

            SendMessageRequest messageRequest = SendMessageRequest.builder()
                .channelId(channelId)
                .content(content)
                .type(ChannelMessageType.NOTICE)
                .build();

            chatServiceClient.sendMessage(requesterId, messageRequest);
        } catch (Exception e) {
            // Log error
        }
    }

    private void produceChannelUpdatedEvent(Channel channel, UUID adminId) {
        UserResponse admin = userServiceClient.getUserById(adminId);
        String adminName = "A user";
        if (admin != null) {
            adminName = admin.getFirstname() + " " + admin.getLastname();
        }

        ChannelUpdatedPayload event = ChannelUpdatedPayload.builder()
            .eventType(com.nnson128.chatapps_base.models.events.channel.ChannelEventType.CHANNEL_UPDATED)
            .channelId(channel.getId())
            .newChannelName(channel.getChannelName())
            .updaterId(adminId)
            .updaterName(adminName)
            .updatedAt(java.time.LocalDateTime.now().toString())
            .memberIds(getParticipantIdsByChannelId(channel.getId()))
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.CHAT_NOTIFICATIONS, event);
        } catch (Exception e) {
            // Log error
        }
    }
}