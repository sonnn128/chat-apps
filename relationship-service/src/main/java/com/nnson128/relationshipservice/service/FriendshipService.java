package com.nnson128.relationshipservice.service;

import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.EventWrapper;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestAcceptedPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestRejectedPayload;
import com.nnson128.chatapps_base.models.events.friendship.payloads.FriendRequestSentPayload;
import com.nnson128.relationshipservice.client.UserServiceClient;
import com.nnson128.relationshipservice.client.ChatServiceClient;
import com.nnson128.relationshipservice.dto.request.SendMessageRequest;
import com.nnson128.relationshipservice.dto.message.ChannelMessageType;
import com.nnson128.relationshipservice.dto.response.FriendRequestResponse;
import com.nnson128.relationshipservice.dto.response.FriendResponse;
import com.nnson128.chatapps_base.dto.res.UserResponse;
import com.nnson128.relationshipservice.model.friendship.Friendship;
import com.nnson128.relationshipservice.model.friendship.FriendshipKey;
import com.nnson128.relationshipservice.model.friendship.FriendshipStatus;
import com.nnson128.relationshipservice.model.channel.Channel;
import com.nnson128.relationshipservice.model.membership.Membership;
import com.nnson128.relationshipservice.model.membership.MembershipKey;
import com.nnson128.relationshipservice.model.membership.MembershipRole;
import com.nnson128.relationshipservice.repository.FriendshipRepository;
import com.nnson128.relationshipservice.repository.ChannelRepository;
import com.nnson128.relationshipservice.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final MessageProducerService messageProducerService;
    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;
    private final ChannelRepository channelRepository;
    private final MembershipRepository membershipRepository;

    public Friendship sendFriendRequest(UUID requesterId, UUID friendId) {

        // Create friendship key
        FriendshipKey key = FriendshipKey.builder()
            .requesterId(requesterId)
            .friendId(friendId)
            .build();

        // Create friendship entity
        Friendship friendship = Friendship.builder()
            .friendshipKey(key)
            .status(FriendshipStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // Save to database
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Produce event for real-time notification
        produceFriendRequestSentEvent(savedFriendship);

        return savedFriendship;
    }

    public Friendship acceptFriendRequest(UUID requesterId, UUID accepterId) {

        // Find the pending friendship
        FriendshipKey key = FriendshipKey.builder()
            .requesterId(requesterId)
            .friendId(accepterId)
            .build();

        Friendship friendship = friendshipRepository.findById(key)
            .orElseThrow(() -> new RuntimeException("Friend request not found"));

        // Update status to accepted
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());

        // Save updated friendship
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Create direct message channel
        createDirectChannelForNewFriends(requesterId, accepterId);

        // Produce event for real-time notification
        produceFriendRequestAcceptedEvent(savedFriendship);

        return savedFriendship;
    }

    private void createDirectChannelForNewFriends(UUID user1, UUID user2) {
        // Sort UUIDs to ensure consistent channel creation
        UUID sortedUser1 = user1.compareTo(user2) < 0 ? user1 : user2;
        UUID sortedUser2 = user1.compareTo(user2) < 0 ? user2 : user1;

        // Check if direct channel already exists
        List<UUID> existingChannels = membershipRepository.findDirectChannelIds(sortedUser1, sortedUser2);
        if (!existingChannels.isEmpty()) {
            return; // Channel already exists
        }

        // Create new direct channel
        Channel newChannel = Channel.builder()
            .channelName(null)
            .channelType(Channel.DIRECT_MESSAGE)
            .build();
        Channel savedChannel = channelRepository.save(newChannel);

        // Create memberships for both users
        Membership membership1 = Membership.builder()
            .membershipKey(MembershipKey.builder()
                .channelId(savedChannel.getId())
                .userId(sortedUser1)
                .build())
            .role(MembershipRole.MEMBER)
            .build();

        Membership membership2 = Membership.builder()
            .membershipKey(MembershipKey.builder()
                .channelId(savedChannel.getId())
                .userId(sortedUser2)
                .build())
            .role(MembershipRole.MEMBER)
            .build();

        membershipRepository.saveAll(List.of(membership1, membership2));

        // Create notice message
        try {
            SendMessageRequest noticeRequest = SendMessageRequest.builder()
                .channelId(savedChannel.getId())
                .content("You are connected on messenger")
                .type(ChannelMessageType.NOTICE)
                .build();
            chatServiceClient.sendMessage(sortedUser1, noticeRequest);
        } catch (Exception e) {
            System.out.println("⚠️ FriendshipService: Failed to create notice message: " + e.getMessage());
        }
    }

    public void rejectFriendRequest(UUID requesterId, UUID rejecterId) {

        // Find the pending friendship - requesterId is who sent the request, rejecterId is who received it
        FriendshipKey key = FriendshipKey.builder()
            .requesterId(requesterId)
            .friendId(rejecterId)
            .build();


        // Check if friendship exists
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(key);
        Friendship friendship;
        if (friendshipOpt.isEmpty()) {
            // Try to find in reverse direction
            FriendshipKey reverseKey = FriendshipKey.builder()
                .requesterId(rejecterId)
                .friendId(requesterId)
                .build();

            Optional<Friendship> reverseFriendshipOpt = friendshipRepository.findById(reverseKey);
            if (reverseFriendshipOpt.isEmpty()) {
                throw new RuntimeException("Friend request not found");
            }
            friendship = reverseFriendshipOpt.get();
        } else {
            friendship = friendshipOpt.get();
        }


        // Delete the friendship (reject)
        friendshipRepository.delete(friendship);

        // Produce event for real-time notification
        produceFriendRequestRejectedEvent(requesterId, rejecterId);
    }

    public void removeFriend(UUID userId, UUID friendId) {

        // Find and delete friendship in both directions
        FriendshipKey key1 = FriendshipKey.builder()
            .requesterId(userId)
            .friendId(friendId)
            .build();

        FriendshipKey key2 = FriendshipKey.builder()
            .requesterId(friendId)
            .friendId(userId)
            .build();

        friendshipRepository.deleteById(key1);
        friendshipRepository.deleteById(key2);

    }

    public List<Friendship> getFriends(UUID userId) {
        return friendshipRepository.findByFriendshipKey_RequesterIdAndStatusOrFriendshipKey_FriendIdAndStatus(
            userId, FriendshipStatus.ACCEPTED, userId, FriendshipStatus.ACCEPTED);
    }

    public List<FriendResponse> getFriendsWithUserInfo(UUID userId) {
        List<Friendship> friendships = friendshipRepository.findByFriendshipKey_RequesterIdAndStatusOrFriendshipKey_FriendIdAndStatus(
            userId, FriendshipStatus.ACCEPTED, userId, FriendshipStatus.ACCEPTED);

        return friendships.stream()
            .map(friendship -> {
                // Determine which user is the friend (not the current user)
                UUID friendId = friendship.getFriendshipKey().getRequesterId().equals(userId)
                    ? friendship.getFriendshipKey().getFriendId()
                    : friendship.getFriendshipKey().getRequesterId();


                try {
                    UserResponse user = userServiceClient.getUserById(friendId);

                    return FriendResponse.builder()
                        .friendId(friendId)
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .avatar(user.getAvatarUrl())
                        .createdAt(friendship.getCreatedAt())
                        .acceptedAt(friendship.getAcceptedAt())
                        .status(friendship.getStatus().toString())
                        .build();
                } catch (Exception e) {
                    return FriendResponse.builder()
                        .friendId(friendId)
                        .firstname("Unknown")
                        .lastname("User")
                        .email("unknown@example.com")
                        .avatar(null)
                        .createdAt(friendship.getCreatedAt())
                        .acceptedAt(friendship.getAcceptedAt())
                        .status(friendship.getStatus().toString())
                        .build();
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }

    public List<Friendship> getPendingRequests(UUID userId) {
        return friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    public List<FriendRequestResponse> getPendingRequestsWithUserInfo(UUID userId) {
        List<Friendship> pendingRequests = friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);

        return pendingRequests.stream()
            .map(friendship -> {
                UUID requesterId = friendship.getFriendshipKey().getRequesterId();

                try {
                    UserResponse user = userServiceClient.getUserById(requesterId);

                    return FriendRequestResponse.builder()
                        .requesterId(requesterId)
                        .requesterFirstname(user.getFirstname())
                        .requesterLastname(user.getLastname())
                        .requesterEmail(user.getEmail())
                        .requesterAvatar(user.getAvatarUrl())
                        .createdAt(friendship.getCreatedAt())
                        .status(friendship.getStatus().toString())
                        .build();
                } catch (Exception e) {
                    return FriendRequestResponse.from(friendship);
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private void produceFriendRequestSentEvent(Friendship friendship) {
        // Get user names
        UserResponse requesterResponse = userServiceClient.getUserById(friendship.getFriendshipKey().getRequesterId());
        UserResponse friendResponse = userServiceClient.getUserById(friendship.getFriendshipKey().getFriendId());

        String requesterName = "A user";
        String friendName = "A user";

        if (requesterResponse != null) {
            requesterName = requesterResponse.getFirstname() + " " + requesterResponse.getLastname();
        }
        if (friendResponse != null) {
            friendName = friendResponse.getFirstname() + " " + friendResponse.getLastname();
        }

        FriendRequestSentPayload event = FriendRequestSentPayload.builder()
            .senderId(friendship.getFriendshipKey().getRequesterId())
            .senderName(requesterName)
            .recipientId(friendship.getFriendshipKey().getFriendId())
            .recipientName(friendName)
            .sentAt(friendship.getCreatedAt())
            .build();

        EventWrapper<?> wrapper = EventWrapper.builder()
            .eventType("FRIEND_REQUEST_SENT")
            .eventId(java.util.UUID.randomUUID().toString())
            .timestamp(java.time.LocalDateTime.now())
            .payload(event)
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.FRIENDSHIP_EVENTS, wrapper);
        } catch (Exception e) {
            // Log error
        }
    }

    private void produceFriendRequestAcceptedEvent(Friendship friendship) {
        // Get user names
        UserResponse friend1Response = userServiceClient.getUserById(friendship.getFriendshipKey().getRequesterId());
        UserResponse friend2Response = userServiceClient.getUserById(friendship.getFriendshipKey().getFriendId());

        String friend1Name = "A user";
        String friend2Name = "A user";

        if (friend1Response != null) {
            friend1Name = friend1Response.getFirstname() + " " + friend1Response.getLastname();
        }
        if (friend2Response != null) {
            friend2Name = friend2Response.getFirstname() + " " + friend2Response.getLastname();
        }

        FriendRequestAcceptedPayload event = FriendRequestAcceptedPayload.builder()
            .friend1Id(friendship.getFriendshipKey().getRequesterId())
            .friend1Name(friend1Name)
            .friend2Id(friendship.getFriendshipKey().getFriendId())
            .friend2Name(friend2Name)
            .acceptedAt(friendship.getAcceptedAt())
            .build();

        EventWrapper<?> wrapper = EventWrapper.builder()
            .eventType("FRIEND_REQUEST_ACCEPTED")
            .eventId(java.util.UUID.randomUUID().toString())
            .timestamp(java.time.LocalDateTime.now())
            .payload(event)
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.FRIENDSHIP_EVENTS, wrapper);
        } catch (Exception e) {
            // Log error
        }
    }

    private void produceFriendRequestRejectedEvent(UUID requesterId, UUID rejecterId) {
        // Get user names
        UserResponse requesterResponse = userServiceClient.getUserById(requesterId);
        UserResponse rejectorResponse = userServiceClient.getUserById(rejecterId);

        String requesterName = "A user";
        String rejectorName = "A user";

        if (requesterResponse != null) {
            requesterName = requesterResponse.getFirstname() + " " + requesterResponse.getLastname();
        }
        if (rejectorResponse != null) {
            rejectorName = rejectorResponse.getFirstname() + " " + rejectorResponse.getLastname();
        }

        FriendRequestRejectedPayload event = FriendRequestRejectedPayload.builder()
            .senderId(requesterId)
            .senderName(requesterName)
            .recipientId(rejecterId)
            .recipientName(rejectorName)
            .rejectedAt(LocalDateTime.now())
            .build();

        EventWrapper<?> wrapper = EventWrapper.builder()
            .eventType("FRIEND_REQUEST_REJECTED")
            .eventId(java.util.UUID.randomUUID().toString())
            .timestamp(java.time.LocalDateTime.now())
            .payload(event)
            .build();

        try {
            messageProducerService.sendMessage(KafkaTopics.FRIENDSHIP_EVENTS, wrapper);
        } catch (Exception e) {
            // Log error
        }
    }

    public void cancelFriendRequest(UUID requesterId, UUID friendId) {
        // Find the pending friendship - requesterId is who sent the request, friendId is who received it
        FriendshipKey key = FriendshipKey.builder()
            .requesterId(requesterId)
            .friendId(friendId)
            .build();


        // Check if friendship exists
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(key);
        if (friendshipOpt.isEmpty()) {
            throw new RuntimeException("Friend request not found");
        }

        Friendship friendship = friendshipOpt.get();
        friendshipRepository.delete(friendship);
    }

    public void unfriendUser(UUID userId, UUID friendId) {

        // Find the friendship in both directions
        FriendshipKey key1 = FriendshipKey.builder()
            .requesterId(userId)
            .friendId(friendId)
            .build();

        FriendshipKey key2 = FriendshipKey.builder()
            .requesterId(friendId)
            .friendId(userId)
            .build();

        // Try to find and delete friendship in either direction
        Optional<Friendship> friendship1 = friendshipRepository.findById(key1);
        Optional<Friendship> friendship2 = friendshipRepository.findById(key2);

        if (friendship1.isPresent()) {
            friendshipRepository.delete(friendship1.get());
        } else if (friendship2.isPresent()) {
            friendshipRepository.delete(friendship2.get());
        } else {
            throw new RuntimeException("Friendship not found");
        }

    }
}