package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.request.AddPeopleToChannelRequest;
import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.AddPeopleResponse;
import com.sonnguyen.channelservice.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<?> createChannel(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateChannelRequest request) {
        return ResponseEntity.ok().body(ApiResponse.builder()
                .success(true)
                .message("Channel created successfully")
                .data(channelService.createChannel(request, UUID.fromString(userId)))
                .build());
    }

    @GetMapping(value = {"", "/"})
    public ResponseEntity<?> getMyChannels(
            @RequestHeader("X-User-Id") String userId) {
        List<ChannelResponse> channels = channelService.getChannelsWithMessagesForUser(UUID.fromString(userId));

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
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AddPeopleToChannelRequest request) {
        AddPeopleResponse addPeopleResponse = channelService.addPeopleToChannel(channelId, UUID.fromString(userId), request.getMemberIds());
        ApiResponse<AddPeopleResponse> response = ApiResponse.<AddPeopleResponse>builder()
                .success(true)
                .message("People added to channel successfully")
                .data(addPeopleResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}

