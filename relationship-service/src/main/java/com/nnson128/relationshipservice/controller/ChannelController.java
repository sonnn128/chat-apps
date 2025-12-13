package com.nnson128.relationshipservice.controller;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.relationshipservice.dto.request.CreateChannelRequest;
import com.nnson128.relationshipservice.dto.request.AddPeopleToChannelRequest;
import com.nnson128.relationshipservice.dto.request.CreateDirectChannelRequest;
import com.nnson128.relationshipservice.dto.response.ChannelResponse;
import com.nnson128.relationshipservice.dto.response.AddPeopleResponse;
import com.nnson128.relationshipservice.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<?> createChannel(
                    @AuthenticationPrincipal Jwt jwt,
                    @RequestBody CreateChannelRequest request) {
            UUID userId = UUID.fromString(jwt.getSubject());
            ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Channel created successfully")
                .data(channelService.createChannel(request, userId))
                .build();
            return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = {"", "/"})
        public ResponseEntity<?> getMyChannels(
                        @AuthenticationPrincipal Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                List<ChannelResponse> channels = channelService.getChannelsWithMessagesForUser(userId);

        ApiResponse<List<ChannelResponse>> response = ApiResponse.<List<ChannelResponse>>builder()
                .success(true)
                .message("Get all channels with messages successfully")
                .data(channels)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{channelId}/participants/{userId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkUserIsParticipant(
            @PathVariable UUID channelId,
            @PathVariable UUID userId) {
        
        boolean isParticipant = channelService.isUserParticipant(channelId, userId);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message("User participant check completed")
                .data(isParticipant)
                .build();
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{channelId}/participants/ids")
    public ResponseEntity<ApiResponse<List<UUID>>> getParticipantIdsByChannelId(@PathVariable UUID channelId) {
        List<UUID> participantIds = channelService.getParticipantIdsByChannelId(channelId);
        ApiResponse<List<UUID>> response = ApiResponse.<List<UUID>>builder()
                .success(true)
                .message("Participant IDs retrieved successfully")
                .data(participantIds)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/ids")
    public ResponseEntity<ApiResponse<List<UUID>>> getChannelIdsByUserId(@PathVariable UUID userId) {
        List<UUID> channelIds = channelService.getChannelIdsByUserId(userId);
        ApiResponse<List<UUID>> response = ApiResponse.<List<UUID>>builder()
                .success(true)
                .message("Channel IDs retrieved successfully")
                .data(channelIds)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{channelId}/add-people")
        public ResponseEntity<ApiResponse<AddPeopleResponse>> addPeopleToChannel(
                        @PathVariable UUID channelId,
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody AddPeopleToChannelRequest request) {
                UUID userId = UUID.fromString(jwt.getSubject());
                AddPeopleResponse addPeopleResponse = channelService.addPeopleToChannel(channelId, userId, request.getMemberIds());
        ApiResponse<AddPeopleResponse> response = ApiResponse.<AddPeopleResponse>builder()
                .success(true)
                .message("People added to channel successfully")
                .data(addPeopleResponse)
                .build();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/direct")
    public ResponseEntity<?> createDirectChannel(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateDirectChannelRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Direct channel retrieved/created successfully")
                .data(channelService.findOrCreateDirectChannel(userId, request.getFriendId()))
                .build();
        return ResponseEntity.ok().body(response);
    }
    @DeleteMapping("/{channelId}")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(
            @PathVariable UUID channelId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        channelService.deleteChannel(channelId, userId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Channel deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{channelId}/avatar")
    public ResponseEntity<ApiResponse<ChannelResponse>> updateChannelAvatar(
            @PathVariable UUID channelId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String avatarUrl = request.get("avatarUrl");
        
        ChannelResponse updatedChannel = channelService.updateChannelAvatar(channelId, avatarUrl, userId);
        
        ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Channel avatar updated successfully")
                .data(updatedChannel)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{channelId}/name")
    public ResponseEntity<ApiResponse<ChannelResponse>> updateChannelName(
            @PathVariable UUID channelId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String channelName = request.get("channelName");
        
        ChannelResponse updatedChannel = channelService.updateChannelName(channelId, channelName, userId);
        
        ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Channel name updated successfully")
                .data(updatedChannel)
                .build();
        
        return ResponseEntity.ok(response);
    }
}
