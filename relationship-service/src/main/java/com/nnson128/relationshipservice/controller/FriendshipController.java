package com.nnson128.relationshipservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.relationshipservice.dto.response.FriendRequestResponse;
import com.nnson128.relationshipservice.dto.response.FriendResponse;
import com.nnson128.relationshipservice.model.friendship.Friendship;
import com.nnson128.relationshipservice.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{friendId}")
    public ResponseEntity<?> sendFriendRequest(
        @PathVariable UUID friendId,
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        Friendship friendship = friendshipService.sendFriendRequest(authenticatedUserId, friendId);
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
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        Friendship friendship = friendshipService.acceptFriendRequest(friendId, authenticatedUserId);
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
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        friendshipService.removeFriend(authenticatedUserId, friendId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("Friend removed successfully")
            .data(null)
            .build();
        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        List<FriendResponse> friends = friendshipService.getFriendsWithUserInfo(authenticatedUserId);
        ApiResponse<List<FriendResponse>> response = ApiResponse.<List<FriendResponse>>builder()
            .success(true)
            .message("Friends retrieved successfully")
            .data(friends)
            .build();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests(
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        List<FriendRequestResponse> pendingRequests = friendshipService.getPendingRequestsWithUserInfo(authenticatedUserId);
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
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        friendshipService.rejectFriendRequest(authenticatedUserId, requesterId);
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
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        friendshipService.cancelFriendRequest(authenticatedUserId, friendId);
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
        @AuthenticationPrincipal Jwt jwt) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        friendshipService.unfriendUser(authenticatedUserId, friendId);
        ApiResponse<String> response = ApiResponse.<String>builder()
            .success(true)
            .message("Friend removed successfully")
            .data("Friend has been removed")
            .build();
        return ResponseEntity.ok(response);
    }

}