package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.client.UserServiceClient;
import com.sonnguyen.channelservice.client.ChatServiceClient;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.dto.message.ChannelMessageDto;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.UserResponse;
import com.sonnguyen.channelservice.dto.response.AddPeopleResponse;
import com.sonnguyen.channelservice.exception.ChannelNotFoundException;
import com.sonnguyen.channelservice.events.producer.EventWrapper;
import com.sonnguyen.channelservice.events.producer.MembersAddedToChannelEvent;
import com.sonnguyen.channelservice.events.producer.ChannelCreatedEvent;
import com.sonnguyen.channelservice.events.producer.AddPeopleEvent;
import com.sonnguyen.channelservice.model.channel.Channel;
import com.sonnguyen.channelservice.model.membership.Membership;
import com.sonnguyen.channelservice.dto.message.ChannelMessageType;
import com.sonnguyen.channelservice.model.membership.MembershipKey;
import com.sonnguyen.channelservice.model.membership.MembershipRole;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import com.sonnguyen.channelservice.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;
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

        // 5. publish event for chat-service to save message and notification service to notify others (exclude sender)
        List<UUID> memberIds = List.of(creatorId);
        produceNewChannelEvent(savedChannel, creatorId, memberIds);

        // 6. return response with notice message (sender gets message immediately via HTTP)
        return ChannelResponse.builder()
                .id(savedChannel.getId())
                .channelName(savedChannel.getChannelName())
                .createdAt(savedChannel.getCreatedAt())
                .message(noticeMessage)
                .memberIds(memberIds)
                .build();
    }

    private ChannelMessageDto createNoticeMessage(Channel channel, UUID creatorId) {
        log.info("🔔 ChannelService: Creating notice message for channel: {}", channel.getId());

        // Generate message ID
        UUID messageId = UUID.randomUUID();

        // Get creator name
        String creatorName = "A user";
        Map<String, Object> creatorResponse = userServiceClient.getUserById(creatorId);
        if (creatorResponse != null && (Boolean) creatorResponse.getOrDefault("success", false)) {
            Map<String, Object> userData = (Map<String, Object>) creatorResponse.get("data");
            if (userData != null) {
                String firstName = (String) userData.get("firstname");
                String lastName = (String) userData.get("lastname");
                if (firstName != null && lastName != null) {
                    creatorName = firstName + " " + lastName;
                }
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
        Map<String, Object> addedByResponse = userServiceClient.getUserById(addedByUserId);
        if (addedByResponse != null && (Boolean) addedByResponse.getOrDefault("success", false)) {
            Map<String, Object> userData = (Map<String, Object>) addedByResponse.get("data");
            if (userData != null) {
                String firstName = (String) userData.get("firstname");
                String lastName = (String) userData.get("lastname");
                if (firstName != null && lastName != null) {
                    addedByUserName = firstName + " " + lastName;
                }
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

        log.info("✅ ChannelService: Created add people notice message: {}", noticeMessage);
        return noticeMessage;
    }

    private void produceAddPeopleEvent(Channel channel, UUID addedByUserId, List<UUID> newMemberIds, List<UserResponse> newMembers) {
        log.info("🔔 ChannelService: Publishing AddPeopleEvent for channel: {}", channel.getId());

        // Get added by user name
        String addedByUserName = "A user";
        try {
            Map<String, Object> addedByResponse = userServiceClient.getUserById(addedByUserId);
            if (addedByResponse != null && (Boolean) addedByResponse.getOrDefault("success", false)) {
                Map<String, Object> userData = (Map<String, Object>) addedByResponse.get("data");
                if (userData != null) {
                    String firstName = (String) userData.get("firstname");
                    String lastName = (String) userData.get("lastname");
                    if (firstName != null && lastName != null) {
                        addedByUserName = firstName + " " + lastName;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ ChannelService: Could not fetch added by user name: {}", e.getMessage());
        }

        // Get new member names
        List<String> newMemberNames = newMembers.stream()
                .map(member -> member.getFirstname() + " " + member.getLastname())
                .collect(java.util.stream.Collectors.toList());

        // Create AddPeopleEvent
        AddPeopleEvent event = AddPeopleEvent.builder()
                .channelId(channel.getId())
                .channelName(channel.getChannelName())
                .addedByUserId(addedByUserId)
                .addedByUserName(addedByUserName)
                .newMemberIds(newMemberIds)
                .newMemberNames(newMemberNames)
                .addedAt(java.time.LocalDateTime.now())
                .build();

        // Publish event
        EventWrapper<AddPeopleEvent> wrapper = new EventWrapper<>("ADD_PEOPLE", event);
        log.info("🔔 ChannelService: Sending AddPeopleEvent: {}", wrapper);
        kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
    }

    private void produceNewChannelEvent(Channel channel, UUID creatorId, List<UUID> memberIds) {
//      1. get name: creator created channel "ABCD"
        Map<String, Object> creatorResponse = userServiceClient.getUserById(creatorId);
        String creatorName = "A user";
        if (creatorResponse != null && (Boolean) creatorResponse.getOrDefault("success", false)) {
            Map<String, Object> userData = (Map<String, Object>) creatorResponse.get("data");
            if (userData != null) {
                String firstName = (String) userData.get("firstname");
                String lastName = (String) userData.get("lastname");
                if (firstName != null && lastName != null) {
                    creatorName = firstName + " " + lastName;
                }
            }
        }
        log.info("🔔 ChannelService: Creator name: {}", creatorName);

//      2. create event
        ChannelCreatedEvent event = ChannelCreatedEvent.builder()
                .channelId(channel.getId())
                .channelName(channel.getChannelName())
                .creatorId(creatorId)
                .creatorName(creatorName)
                .createdAt(channel.getCreatedAt())
                .memberIds(memberIds)
                .build();
        log.info("🔔 ChannelService: ChannelCreatedEvent created: {}", event);

//        3. push event
        EventWrapper<ChannelCreatedEvent> wrapper = new EventWrapper<>(ChannelCreatedEvent.EVENT_TYPE, event);
        log.info("🔔 ChannelService: Sending EventWrapper: {}", wrapper);
        try {
            kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
            log.info("✅ ChannelService: ChannelCreatedEvent sent to topic: {}", NOTIFICATION_TOPIC);
        } catch (Exception e) {
            log.error("❌ ChannelService: Error sending ChannelCreatedEvent: {}", e.getMessage(), e);
        }
    }


    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        log.info("🔍 ChannelService: Getting channels for user: {}", userId);

        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);
        log.info("📋 ChannelService: Found {} memberships for user: {}", memberships.size(), userId);

        List<ChannelResponse> channels = memberships.stream()
                .map(membership -> {
                    UUID channelId = membership.getMembershipKey().getChannelId();
                    return channelRepository.findById(channelId)
                            .map(ChannelResponse::from)
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("✅ ChannelService: Returning {} channels for user: {}", channels.size(), userId);
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
        log.info("📨 ChannelService: Received {} message groups from chat-service", rawMessagesMap.size());

        // Use ChannelMessageDto objects directly
        Map<UUID, List<ChannelMessageDto>> messagesMap = rawMessagesMap;

        // Get all member IDs for all channels
        Map<UUID, List<UUID>> memberIdsMap = getMemberIdsForAllChannels(channelIds);
        log.info("👥 ChannelService: Retrieved member IDs for {} channels", memberIdsMap.size());

        // Build response using stream map
        List<ChannelResponse> channelResponses = channels.stream()
                .map(channel -> {
                    List<ChannelMessageDto> channelMessages = messagesMap.getOrDefault(channel.getId(), List.of());
                    List<UUID> channelMembers = memberIdsMap.getOrDefault(channel.getId(), List.of());

                    log.info("📋 ChannelService: Channel {} has {} messages and {} members",
                            channel.getId(), channelMessages.size(), channelMembers.size());

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

        log.info("✅ ChannelService: Returning {} channels with messages for user: {}", channelResponses.size(), userId);
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
        log.info("👥 ChannelService: Getting member IDs for {} channels", channelIds.size());

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
                                log.error("❌ ChannelService: Error getting members for channel {}: {}", channelId, e.getMessage());
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
        log.info("🔍 ChannelService: Getting channel IDs for user: {}", userId);

        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);
        List<UUID> channelIds = memberships.stream()
                .map(membership -> membership.getMembershipKey().getChannelId())
                .collect(Collectors.toList());

        log.info("✅ ChannelService: Found {} channels for user: {}", channelIds.size(), userId);
        return channelIds;
    }


    public AddPeopleResponse addPeopleToChannel(UUID channelId, UUID addedByUserId, List<UUID> memberIds) {
        log.info("👥 ChannelService: Adding {} people to channel {} by user {}", memberIds.size(), channelId, addedByUserId);

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
            log.info("⚠️ ChannelService: All users are already members of the channel");
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
        log.info("✅ ChannelService: Added {} new members to channel {}", newMemberIds.size(), channelId);

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
            Map<String, Object> addedByResponse = userServiceClient.getUserById(addedByUserId);
            String addedByUserName = "A user";
            if (addedByResponse != null && (Boolean) addedByResponse.getOrDefault("success", false)) {
                Map<String, Object> userData = (Map<String, Object>) addedByResponse.get("data");
                if (userData != null) {
                    String firstName = (String) userData.get("firstname");
                    String lastName = (String) userData.get("lastname");
                    if (firstName != null && lastName != null) {
                        addedByUserName = firstName + " " + lastName;
                    }
                }
            }

            // Get user info for the new members
            List<String> newMemberNames = newMemberIds.stream()
                    .map(memberId -> {
                        try {
                            Map<String, Object> userResponse = userServiceClient.getUserById(memberId);
                            if (userResponse != null && (Boolean) userResponse.getOrDefault("success", false)) {
                                Map<String, Object> userData = (Map<String, Object>) userResponse.get("data");
                                if (userData != null) {
                                    String firstName = (String) userData.get("firstname");
                                    String lastName = (String) userData.get("lastname");
                                    if (firstName != null && lastName != null) {
                                        return firstName + " " + lastName;
                                    }
                                }
                            }
                            return "Unknown User";
                        } catch (Exception e) {
                            log.warn("⚠️ ChannelService: Could not fetch user info for member {}", memberId);
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
                    .content(notificationMessage)
                    .type(ChannelMessageType.NOTICE)
                    .build();

            try {
                ChannelMessageDto response = chatServiceClient.sendMessage(channelId, addedByUserId, messageRequest);
                if (response != null) {
                    log.info("✅ ChannelService: Sent members added notification: {}", notificationMessage);
                } else {
                    log.warn("⚠️ ChannelService: Failed to send notification message: response is null");
                }
            } catch (Exception e) {
                log.error("❌ ChannelService: Error sending notification message: {}", e.getMessage(), e);
            }

            // Send real-time event
            MembersAddedToChannelEvent event = MembersAddedToChannelEvent.builder()
                    .channelId(channelId)
                    .channelName(channelName)
                    .addedByUserId(addedByUserId)
                    .addedByUserName(addedByUserName)
                    .newMemberIds(newMemberIds)
                    .newMemberNames(newMemberNames)
                    .addedAt(java.time.LocalDateTime.now())
                    .build();

            EventWrapper<MembersAddedToChannelEvent> wrapper = new EventWrapper<>(MembersAddedToChannelEvent.EVENT_TYPE, event);
            kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
            log.info("✅ ChannelService: MembersAddedToChannelEvent sent to topic: {}", NOTIFICATION_TOPIC);

        } catch (Exception e) {
            log.error("❌ ChannelService: Error sending members added notification", e);
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
                    Map<String, Object> userResponse = userServiceClient.getUserById(userId);
                    if ((Boolean) userResponse.getOrDefault("success", false)) {
                        Map<String, Object> userData = (Map<String, Object>) userResponse.get("data");
                        if (userData != null) {
                            // Convert Map to UserResponse manually
                            UserResponse user = UserResponse.builder()
                                    .id(UUID.fromString((String) userData.get("id")))
                                    .firstname((String) userData.get("firstname"))
                                    .lastname((String) userData.get("lastname"))
                                    .email((String) userData.get("email"))
                                    .phone((String) userData.get("phone"))
                                    .avatarUrl((String) userData.get("avatarUrl"))
                                    .build();
                            users.add(user);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ ChannelService: Failed to get user info for {}: {}", userId, e.getMessage());
                    // Create a fallback user response
                    users.add(UserResponse.builder()
                            .id(userId)
                            .firstname("User")
                            .lastname(userId.toString().substring(0, 8))
                            .email(userId.toString().substring(0, 8) + "@example.com")
                            .avatarUrl(null)
                            .avatarPublicId(null)
                            .build());
                }
            }
            return users;
        } catch (Exception e) {
            log.error("❌ ChannelService: Error getting detailed user info: {}", e.getMessage());
            return List.of();
        }
    }

}
