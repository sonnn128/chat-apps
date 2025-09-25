package com.sonnguyen.friendshipservice.service;

import com.sonnguyen.friendshipservice.events.dto.EventWrapper;
import com.sonnguyen.friendshipservice.events.dto.FriendRequestAcceptedEvent;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final FriendshipRepository friendshipRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        log.info("✅ FriendshipService: Friend request saved to database");

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

    public List<Friendship> getPendingRequests(UUID userId) {
        log.info("🔔 FriendshipService: Getting pending requests for user: {}", userId);
        return friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);
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
}