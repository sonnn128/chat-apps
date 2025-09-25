package com.sonnguyen.friendshipservice.controller;

import com.sonnguyen.friendshipservice.dto.response.ApiResponse;
import com.sonnguyen.friendshipservice.model.Friendship;
import com.sonnguyen.friendshipservice.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{friendId}")
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable UUID friendId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        Friendship friendship = friendshipService.sendFriendRequest(UUID.fromString(authenticatedUserId), friendId);
        ApiResponse<Friendship> response = ApiResponse.<Friendship>builder()
                .success(true)
                .message("Friend request sent successfully")
                .data(friendship)
                .build();
        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/accept/{friendId}")
    public ResponseEntity<ApiResponse<Friendship>> acceptFriendRequest(
            @PathVariable UUID friendId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Accepting friend request from {} to {}", friendId, authenticatedUserId);
        Friendship friendship = friendshipService.acceptFriendRequest(friendId, UUID.fromString(authenticatedUserId));
        ApiResponse<Friendship> response = ApiResponse.<Friendship>builder()
                .success(true)
                .message("Friend request accepted successfully")
                .data(friendship)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable UUID friendId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Removing friendship between {} and {}", authenticatedUserId, friendId);
        friendshipService.removeFriend(UUID.fromString(authenticatedUserId), friendId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Friend removed successfully")
                .data(null)
                .build();
        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Friendship>>> getFriends(
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Getting friends for user: {}", authenticatedUserId);
        List<Friendship> friends = friendshipService.getFriends(UUID.fromString(authenticatedUserId));
        ApiResponse<List<Friendship>> response = ApiResponse.<List<Friendship>>builder()
                .success(true)
                .message("Friends retrieved successfully")
                .data(friends)
                .build();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Friendship>>> getPendingRequests(
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Getting pending friend requests for user: {}", authenticatedUserId);
        List<Friendship> pendingRequests = friendshipService.getPendingRequests(UUID.fromString(authenticatedUserId));
        ApiResponse<List<Friendship>> response = ApiResponse.<List<Friendship>>builder()
                .success(true)
                .message("Pending requests retrieved successfully")
                .data(pendingRequests)
                .build();
        return ResponseEntity.ok(response);

    }

}