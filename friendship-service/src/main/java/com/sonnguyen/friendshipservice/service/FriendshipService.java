package com.sonnguyen.friendshipservice.service;

import com.sonnguyen.friendshipservice.client.UserServiceClient;
import com.sonnguyen.friendshipservice.dto.response.FriendRequestResponse;
import com.sonnguyen.friendshipservice.dto.response.FriendResponse;
import com.sonnguyen.friendshipservice.dto.response.UserResponse;
import com.sonnguyen.friendshipservice.events.dto.EventWrapper;
import com.sonnguyen.friendshipservice.events.dto.FriendRequestAcceptedEvent;
import com.sonnguyen.friendshipservice.events.dto.FriendRequestRejectedEvent;
import com.sonnguyen.friendshipservice.events.dto.FriendRequestSentEvent;
import com.sonnguyen.friendshipservice.model.Friendship;
import com.sonnguyen.friendshipservice.model.FriendshipKey;
import com.sonnguyen.friendshipservice.model.FriendshipStatus;
import com.sonnguyen.friendshipservice.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final FriendshipRepository friendshipRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;

    public Friendship sendFriendRequest(UUID requesterId, UUID friendId) {
        log.info("🔔 FriendshipService: Sending friend request from {} to {}", requesterId, friendId);

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
        log.info("🔔 FriendshipService: Accepting friend request from {} by {}", requesterId, accepterId);

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
        log.info("✅ FriendshipService: Friend request accepted and saved to database");

        // Produce event for real-time notification
        produceFriendRequestAcceptedEvent(savedFriendship);

        return savedFriendship;
    }

    public void rejectFriendRequest(UUID requesterId, UUID rejecterId) {
        log.info("🔔 FriendshipService: Rejecting friend request from {} by {}", requesterId, rejecterId);

        // Find the pending friendship - requesterId is who sent the request, rejecterId is who received it
        FriendshipKey key = FriendshipKey.builder()
                .requesterId(requesterId)
                .friendId(rejecterId)
                .build();

        log.info("🔍 FriendshipService: Looking for friendship with key: requesterId={}, friendId={}", key.getRequesterId(), key.getFriendId());

        // Check if friendship exists
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(key);
        Friendship friendship;
        if (friendshipOpt.isEmpty()) {
            log.error("❌ FriendshipService: Friend request not found for requesterId={}, friendId={}", requesterId, rejecterId);
            // Try to find in reverse direction
            FriendshipKey reverseKey = FriendshipKey.builder()
                    .requesterId(rejecterId)
                    .friendId(requesterId)
                    .build();
            log.info("🔍 FriendshipService: Trying reverse key: requesterId={}, friendId={}", reverseKey.getRequesterId(), reverseKey.getFriendId());
            
            Optional<Friendship> reverseFriendshipOpt = friendshipRepository.findById(reverseKey);
            if (reverseFriendshipOpt.isEmpty()) {
                log.error("❌ FriendshipService: Friend request not found in both directions");
                throw new RuntimeException("Friend request not found");
            }
            friendship = reverseFriendshipOpt.get();
        } else {
            friendship = friendshipOpt.get();
        }

        log.info("✅ FriendshipService: Found friendship to reject: {}", friendship);

        // Delete the friendship (reject)
        friendshipRepository.delete(friendship);
        log.info("✅ FriendshipService: Friend request rejected and deleted from database");

        // Produce event for real-time notification
        produceFriendRequestRejectedEvent(requesterId, rejecterId);
    }

    public void removeFriend(UUID userId, UUID friendId) {
        log.info("🔔 FriendshipService: Removing friendship between {} and {}", userId, friendId);

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

        log.info("✅ FriendshipService: Friendship removed from database");
    }

    public List<Friendship> getFriends(UUID userId) {
        log.info("🔔 FriendshipService: Getting friends for user: {}", userId);
        return friendshipRepository.findByFriendshipKey_RequesterIdAndStatusOrFriendshipKey_FriendIdAndStatus(
                userId, FriendshipStatus.ACCEPTED, userId, FriendshipStatus.ACCEPTED);
    }

    public List<FriendResponse> getFriendsWithUserInfo(UUID userId) {
        log.info("🔔 FriendshipService: Getting friends with user info for user: {}", userId);
        List<Friendship> friendships = friendshipRepository.findByFriendshipKey_RequesterIdAndStatusOrFriendshipKey_FriendIdAndStatus(
                userId, FriendshipStatus.ACCEPTED, userId, FriendshipStatus.ACCEPTED);
        log.info("📋 FriendshipService: Found {} friendships", friendships.size());
        
        return friendships.stream()
                .map(friendship -> {
                    // Determine which user is the friend (not the current user)
                    UUID friendId = friendship.getFriendshipKey().getRequesterId().equals(userId) 
                            ? friendship.getFriendshipKey().getFriendId() 
                            : friendship.getFriendshipKey().getRequesterId();
                    
                    log.info("🔍 FriendshipService: Fetching user info for friend: {}", friendId);
                    
                    try {
                        UserResponse user = userServiceClient.getUserById(friendId);
                        log.info("✅ FriendshipService: Successfully fetched friend info: {} {}", 
                                user.getFirstname(), user.getLastname());
                        
                        return FriendResponse.builder()
                                .friendId(friendId)
                                .friendFirstname(user.getFirstname())
                                .friendLastname(user.getLastname())
                                .friendEmail(user.getEmail())
                                .friendAvatar(user.getAvatar())
                                .createdAt(friendship.getCreatedAt())
                                .acceptedAt(friendship.getAcceptedAt())
                                .status(friendship.getStatus().toString())
                                .build();
                    } catch (Exception e) {
                        log.error("❌ FriendshipService: Error fetching friend info for friend {}: {}", 
                                friendId, e.getMessage());
                        log.error("❌ FriendshipService: Exception details: ", e);
                        return FriendResponse.builder()
                                .friendId(friendId)
                                .friendFirstname("Unknown")
                                .friendLastname("User")
                                .friendEmail("unknown@example.com")
                                .friendAvatar(null)
                                .createdAt(friendship.getCreatedAt())
                                .acceptedAt(friendship.getAcceptedAt())
                                .status(friendship.getStatus().toString())
                                .build();
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Friendship> getPendingRequests(UUID userId) {
        log.info("🔔 FriendshipService: Getting pending requests for user: {}", userId);
        return friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    public List<FriendRequestResponse> getPendingRequestsWithUserInfo(UUID userId) {
        log.info("🔔 FriendshipService: Getting pending requests with user info for user: {}", userId);
        List<Friendship> pendingRequests = friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);
        log.info("📋 FriendshipService: Found {} pending requests", pendingRequests.size());
        
        return pendingRequests.stream()
                .map(friendship -> {
                    UUID requesterId = friendship.getFriendshipKey().getRequesterId();
                    log.info("🔍 FriendshipService: Fetching user info for requester: {}", requesterId);
                    
                    try {
                        UserResponse user = userServiceClient.getUserById(requesterId);
                        log.info("✅ FriendshipService: Successfully fetched user info: {} {}", 
                                user.getFirstname(), user.getLastname());
                        
                        return FriendRequestResponse.builder()
                                .requesterId(requesterId)
                                .requesterFirstname(user.getFirstname())
                                .requesterLastname(user.getLastname())
                                .requesterEmail(user.getEmail())
                                .requesterAvatar(user.getAvatar())
                                .createdAt(friendship.getCreatedAt())
                                .status(friendship.getStatus().toString())
                                .build();
                    } catch (Exception e) {
                        log.error("❌ FriendshipService: Error fetching user info for requester {}: {}", 
                                requesterId, e.getMessage());
                        log.error("❌ FriendshipService: Exception details: ", e);
                        return FriendRequestResponse.from(friendship);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void produceFriendRequestSentEvent(Friendship friendship) {
        FriendRequestSentEvent event = FriendRequestSentEvent.builder()
                .requesterId(friendship.getFriendshipKey().getRequesterId())
                .friendId(friendship.getFriendshipKey().getFriendId())
                .createdAt(friendship.getCreatedAt())
                .build();

        EventWrapper<FriendRequestSentEvent> wrapper = new EventWrapper<>(FriendRequestSentEvent.EVENT_TYPE, event);
        kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
        log.info("✅ FriendshipService: FriendRequestSentEvent sent to topic: {}", NOTIFICATION_TOPIC);
    }

    private void produceFriendRequestAcceptedEvent(Friendship friendship) {
        FriendRequestAcceptedEvent event = FriendRequestAcceptedEvent.builder()
                .requesterId(friendship.getFriendshipKey().getRequesterId())
                .accepterId(friendship.getFriendshipKey().getFriendId())
                .acceptedAt(friendship.getAcceptedAt())
                .build();

        EventWrapper<FriendRequestAcceptedEvent> wrapper = new EventWrapper<>(FriendRequestAcceptedEvent.EVENT_TYPE, event);
        kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
        log.info("✅ FriendshipService: FriendRequestAcceptedEvent sent to topic: {}", NOTIFICATION_TOPIC);
    }

    private void produceFriendRequestRejectedEvent(UUID requesterId, UUID rejecterId) {
        FriendRequestRejectedEvent event = FriendRequestRejectedEvent.builder()
                .requesterId(requesterId)
                .rejecterId(rejecterId)
                .rejectedAt(LocalDateTime.now())
                .build();

        EventWrapper<FriendRequestRejectedEvent> wrapper = new EventWrapper<>(FriendRequestRejectedEvent.EVENT_TYPE, event);
        kafkaTemplate.send(NOTIFICATION_TOPIC, wrapper);
        log.info("✅ FriendshipService: FriendRequestRejectedEvent sent to topic: {}", NOTIFICATION_TOPIC);
    }

    public void cancelFriendRequest(UUID requesterId, UUID friendId) {
        log.info("🔔 FriendshipService: Cancelling friend request from {} to {}", requesterId, friendId);

        // Find the pending friendship - requesterId is who sent the request, friendId is who received it
        FriendshipKey key = FriendshipKey.builder()
                .requesterId(requesterId)
                .friendId(friendId)
                .build();

        log.info("🔍 FriendshipService: Looking for friendship with key: requesterId={}, friendId={}", key.getRequesterId(), key.getFriendId());

        // Check if friendship exists
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(key);
        if (friendshipOpt.isEmpty()) {
            log.error("❌ FriendshipService: Friend request not found for requesterId={}, friendId={}", requesterId, friendId);
            throw new RuntimeException("Friend request not found");
        }

        Friendship friendship = friendshipOpt.get();
        log.info("✅ FriendshipService: Found friendship to cancel: {}", friendship);

        // Delete the friendship (cancel)
        friendshipRepository.delete(friendship);
        log.info("✅ FriendshipService: Friend request cancelled and deleted from database");

        // No need to send notification for cancellation - it's a private action
    }

    public void unfriendUser(UUID userId, UUID friendId) {
        log.info("🔔 FriendshipService: Unfriending user {} by {}", friendId, userId);

        // Find the friendship in both directions
        FriendshipKey key1 = FriendshipKey.builder()
                .requesterId(userId)
                .friendId(friendId)
                .build();

        FriendshipKey key2 = FriendshipKey.builder()
                .requesterId(friendId)
                .friendId(userId)
                .build();

        log.info("🔍 FriendshipService: Looking for friendship with keys: {}/{} and {}/{}", 
                key1.getRequesterId(), key1.getFriendId(), key2.getRequesterId(), key2.getFriendId());

        // Try to find and delete friendship in either direction
        Optional<Friendship> friendship1 = friendshipRepository.findById(key1);
        Optional<Friendship> friendship2 = friendshipRepository.findById(key2);

        if (friendship1.isPresent()) {
            friendshipRepository.delete(friendship1.get());
            log.info("✅ FriendshipService: Deleted friendship with key1: {}/{}", key1.getRequesterId(), key1.getFriendId());
        } else if (friendship2.isPresent()) {
            friendshipRepository.delete(friendship2.get());
            log.info("✅ FriendshipService: Deleted friendship with key2: {}/{}", key2.getRequesterId(), key2.getFriendId());
        } else {
            log.error("❌ FriendshipService: Friendship not found between {} and {}", userId, friendId);
            throw new RuntimeException("Friendship not found");
        }

        log.info("✅ FriendshipService: User unfriended successfully");
        // No need to send notification for unfriending - it's a private action
    }
}