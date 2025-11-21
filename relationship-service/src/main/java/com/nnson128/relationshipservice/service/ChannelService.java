package com.nnson128.relationshipservice.service;

import com.nnson128.relationshipservice.client.UserServiceClient;
import com.nnson128.relationshipservice.client.ChatServiceClient;
import com.nnson128.relationshipservice.dto.request.CreateChannelRequest;
import com.nnson128.relationshipservice.dto.request.SendMessageRequest;
import com.nnson128.relationshipservice.dto.message.ChannelMessageDto;
import com.nnson128.relationshipservice.dto.response.ChannelResponse;
import com.nnson128.chatapps_base.dto.res.UserResponse;
import com.nnson128.relationshipservice.dto.response.AddPeopleResponse;
import com.nnson128.relationshipservice.exception.ChannelNotFoundException;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.channel.payloads.ChannelCreatedPayload;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            System.out.println("✅ ChannelService: Notice message saved to database for channel: " + savedChannel.getId());
        } catch (Exception e) {
            System.out.println("⚠️ ChannelService: Failed to save notice message: " + e.getMessage());
        }

        // 5. publish event for chat-service to save message and notification service to notify others (exclude sender)
        List<UUID> memberIds = List.of(creatorId);
        produceNewChannelEvent(savedChannel, creatorId, memberIds);

        // 6. Get detailed user info for the creator
        List<UserResponse> participants = getDetailedUserInfo(memberIds);

        // 7. return response with notice message (sender gets message immediately via HTTP)
        return ChannelResponse.builder()
                .id(savedChannel.getId())
                .channelName(savedChannel.getChannelName())
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
        ChannelMessageDto noticeMessage = ChannelMessageDto.createNoticeMessage(
                channel.getId(),
                messageId,
                creatorId,
                content
        );

        return noticeMessage;
    }

    private ChannelMessageDto createAddPeopleNoticeMessage(Channel channel, UUID addedByUserId, List<UserResponse> newMembers) {
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

    private void produceAddPeopleEvent(Channel channel, UUID addedByUserId, List<UUID> newMemberIds, List<UserResponse> newMembers) {
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

        List<ChannelResponse> channels = memberships.stream()
                .map(membership -> {
                    UUID channelId = membership.getMembershipKey().getChannelId();
                    return channelRepository.findById(channelId)
                            .map(ChannelResponse::from)
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return channels;
    }
//
    public List<ChannelResponse> getChannelsWithMessagesForUser(UUID userId) {
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);

        // Get all channel IDs
        List<UUID> channelIds = memberships.stream()
                .map(membership -> membership.getMembershipKey().getChannelId())
                .collect(Collectors.toList());

        // Get all channels
        List<Channel> channels = channelRepository.findAllById(channelIds);

        // Get all messages for all channels in one API call
        Map<UUID, List<ChannelMessageDto>> rawMessagesMap = chatServiceClient.getBatchChannelMessages(channelIds);

        // Use ChannelMessageDto objects directly
        Map<UUID, List<ChannelMessageDto>> messagesMap = rawMessagesMap;

        // Get all member IDs for all channels
        Map<UUID, List<UUID>> memberIdsMap = getMemberIdsForAllChannels(channelIds);

        // Build response using stream map
        List<ChannelResponse> channelResponses = channels.stream()
                .map(channel -> {
                    List<ChannelMessageDto> channelMessages = messagesMap.getOrDefault(channel.getId(), List.of());
                    List<UUID> channelMembers = memberIdsMap.getOrDefault(channel.getId(), List.of());

                    // Get detailed user info for participants
                    List<UserResponse> participants = getDetailedUserInfo(channelMembers);

                    return ChannelResponse.builder()
                            .id(channel.getId())
                            .channelName(channel.getChannelName())
                            .createdAt(channel.getCreatedAt())
                            .messages(channelMessages)
                            .memberIds(channelMembers)
                            .participants(participants)
                            .build();
                })
                .collect(Collectors.toList());

        return channelResponses;
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
        List<UUID> channelIds = memberships.stream()
                .map(membership -> membership.getMembershipKey().getChannelId())
                .collect(Collectors.toList());

        return channelIds;
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
        List<UserResponse> newMembers = getDetailedUserInfo(newMemberIds);

        // Create notice message for immediate response to sender
        ChannelMessageDto noticeMessage = createAddPeopleNoticeMessage(channel, addedByUserId, newMembers);

        // Send notification message and real-time event
        sendMembersAddedNotification(channelId, addedByUserId, newMemberIds, channel.getChannelName());

        // Publish event for chat service to save notice message
        produceAddPeopleEvent(channel, addedByUserId, newMemberIds, newMembers);

        return AddPeopleResponse.builder()
                .channelId(channelId)
                .channelName(channel.getChannelName())
                .newMembers(newMembers)
                .message(noticeMessage)
                .build();
    }

    private void sendMembersAddedNotification(UUID channelId, UUID addedByUserId, List<UUID> newMemberIds, String channelName) {
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
                ChannelMessageDto response = chatServiceClient.sendMessage(addedByUserId, messageRequest);
                if (response != null) {
                } else {
                }
            } catch (Exception e) {
            }

            // Event is already sent by produceAddPeopleEvent()

        } catch (Exception e) {
        }
    }

    /**
     * Get detailed user information for a list of user IDs
     */
    private List<UserResponse> getDetailedUserInfo(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        try {
            // Call user-service to get detailed user info
            List<UserResponse> users = new ArrayList<>();
            for (UUID userId : userIds) {
                try {
                    UserResponse userResponse = userServiceClient.getUserById(userId);
                    if (userResponse != null) {
                        users.add(userResponse);
                    }
                } catch (Exception e) {
                    // Create a fallback user response
                    users.add(UserResponse.builder()
                            .id(userId)
                            .firstname("User")
                            .lastname(userId.toString().substring(0, 8))
                            .email(userId.toString().substring(0, 8) + "@example.com")
                            .avatarUrl(null)
                            .build());
                }
            }
            return users;
        } catch (Exception e) {
            return List.of();
        }
    }

    public ChannelResponse findOrCreateDirectChannel(UUID user1, UUID user2) {
        // 1. Check if direct channel exists
        List<UUID> existingChannelIds = membershipRepository.findDirectChannelIds(user1, user2);
        if (!existingChannelIds.isEmpty()) {
            UUID channelId = existingChannelIds.get(0);
            Channel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));
            
            // Get messages and members
            List<ChannelMessageDto> messages = chatServiceClient.getChannelMessages(channelId);
            List<UUID> memberIds = List.of(user1, user2);
            List<UserResponse> participants = getDetailedUserInfo(memberIds);

            return ChannelResponse.builder()
                    .id(channel.getId())
                    .channelName(channel.getChannelName())
                    .createdAt(channel.getCreatedAt())
                    .messages(messages)
                    .memberIds(memberIds)
                    .participants(participants)
                    .build();
        }

        // 2. Create new channel
        Channel newChannel = Channel.builder()
                .channelName(null) // Direct chat usually doesn't have a name, or we can set it dynamically
                .build();
        Channel savedChannel = channelRepository.save(newChannel);

        // 3. Create memberships
        Membership membership1 = Membership.builder()
                .membershipKey(MembershipKey.builder().channelId(savedChannel.getId()).userId(user1).build())
                .role(MembershipRole.MEMBER)
                .build();
        
        Membership membership2 = Membership.builder()
                .membershipKey(MembershipKey.builder().channelId(savedChannel.getId()).userId(user2).build())
                .role(MembershipRole.MEMBER)
                .build();

        membershipRepository.saveAll(List.of(membership1, membership2));

        // 4. Return response
        List<UUID> memberIds = List.of(user1, user2);
        List<UserResponse> participants = getDetailedUserInfo(memberIds);

        return ChannelResponse.builder()
                .id(savedChannel.getId())
                .channelName(null)
                .createdAt(savedChannel.getCreatedAt())
                .messages(new ArrayList<>())
                .memberIds(memberIds)
                .participants(participants)
                .build();
    }

}
