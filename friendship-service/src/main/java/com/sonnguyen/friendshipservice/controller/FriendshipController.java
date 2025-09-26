package com.sonnguyen.friendshipservice.controller;

import com.sonnguyen.friendshipservice.dto.response.ApiResponse;
import com.sonnguyen.friendshipservice.dto.response.FriendRequestResponse;
import com.sonnguyen.friendshipservice.dto.response.FriendResponse;
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
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Getting friends for user: {}", authenticatedUserId);
        List<FriendResponse> friends = friendshipService.getFriendsWithUserInfo(UUID.fromString(authenticatedUserId));
        ApiResponse<List<FriendResponse>> response = ApiResponse.<List<FriendResponse>>builder()
                .success(true)
                .message("Friends retrieved successfully")
                .data(friends)
                .build();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests(
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Getting pending friend requests for user: {}", authenticatedUserId);
        List<FriendRequestResponse> pendingRequests = friendshipService.getPendingRequestsWithUserInfo(UUID.fromString(authenticatedUserId));
        ApiResponse<List<FriendRequestResponse>> response = ApiResponse.<List<FriendRequestResponse>>builder()
                .success(true)
                .message("Pending requests retrieved successfully")
                .data(pendingRequests)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reject/{requesterId}")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(
            @PathVariable UUID requesterId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Rejecting friend request from {} by {}", requesterId, authenticatedUserId);
        friendshipService.rejectFriendRequest(UUID.fromString(authenticatedUserId), requesterId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Friend request rejected successfully")
                .data("Friend request has been rejected")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel/{friendId}")
    public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
            @PathVariable UUID friendId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Cancelling friend request to {} by {}", friendId, authenticatedUserId);
        friendshipService.cancelFriendRequest(UUID.fromString(authenticatedUserId), friendId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Friend request cancelled successfully")
                .data("Friend request has been cancelled")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unfriend/{friendId}")
    public ResponseEntity<ApiResponse<String>> unfriendUser(
            @PathVariable UUID friendId,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Unfriending user {} by {}", friendId, authenticatedUserId);
        friendshipService.unfriendUser(UUID.fromString(authenticatedUserId), friendId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Friend removed successfully")
                .data("Friend has been removed")
                .build();
        return ResponseEntity.ok(response);
    }

}