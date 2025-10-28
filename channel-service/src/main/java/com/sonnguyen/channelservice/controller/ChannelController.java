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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

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
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody CreateChannelRequest request) {
                UUID userId = UUID.fromString(jwt.getSubject());
                ApiResponse response = new ApiResponse();
                response.setSuccess(true);
                response.setMessage("Channel created successfully");
                response.setData(channelService.createChannel(request, userId));
                return ResponseEntity.ok().body(response);
        }

    @GetMapping(value = {"", "/"})
        public ResponseEntity<?> getMyChannels(
                        @AuthenticationPrincipal Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                List<ChannelResponse> channels = channelService.getChannelsWithMessagesForUser(userId);

        ApiResponse<List<ChannelResponse>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Get all channels with messages successfully");
        response.setData(channels);
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
        ApiResponse<List<UUID>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Participant IDs retrieved successfully");
        response.setData(participantIds);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/ids")
    public ResponseEntity<ApiResponse<List<UUID>>> getChannelIdsByUserId(@PathVariable UUID userId) {
        List<UUID> channelIds = channelService.getChannelIdsByUserId(userId);
        ApiResponse<List<UUID>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Channel IDs retrieved successfully");
        response.setData(channelIds);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{channelId}/add-people")
        public ResponseEntity<ApiResponse<AddPeopleResponse>> addPeopleToChannel(
                        @PathVariable UUID channelId,
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody AddPeopleToChannelRequest request) {
                UUID userId = UUID.fromString(jwt.getSubject());
                AddPeopleResponse addPeopleResponse = channelService.addPeopleToChannel(channelId, userId, request.getMemberIds());
        ApiResponse<AddPeopleResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("People added to channel successfully");
        response.setData(addPeopleResponse);
        return ResponseEntity.ok(response);
    }
}

