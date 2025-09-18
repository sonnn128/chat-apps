package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.client.ChatServiceClient;
import com.sonnguyen.channelservice.dto.request.AddMembersRequest;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.CreateChannelResponse;
import com.sonnguyen.channelservice.dto.response.MessageResponse;
import com.sonnguyen.channelservice.model.ChannelParticipant;
import com.sonnguyen.channelservice.repository.ChannelParticipantRepository;
import com.sonnguyen.channelservice.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final ChatServiceClient chatServiceClient;
    private final ChannelParticipantRepository channelParticipantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChannelResponse>> createChannel(
            @RequestBody CreateChannelRequest request,
            @RequestHeader("X-Authenticated-User-Id") String creatorIdStr) {

        UUID creatorId = UUID.fromString(creatorIdStr);
        CreateChannelResponse newChannel = channelService.createChannel(request, creatorId);

        ApiResponse<CreateChannelResponse> response = ApiResponse.<CreateChannelResponse>builder()
                .success(true)
                .message("Channel created successfully")
                .data(newChannel)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{channelId}/participants/{userId}/check")
    public ResponseEntity<Void> checkUserIsParticipant(
            @PathVariable UUID channelId,
            @PathVariable UUID userId) {

        boolean isParticipant = channelService.isUserParticipant(channelId, userId);
        log.warn("User {} has been checked for participant", isParticipant);
        return isParticipant
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChannelResponse>>> getMyChannels(
            @RequestHeader("X-Authenticated-User-Id") String userId) {

        List<ChannelResponse> channels = channelService.getChannelsForUser(UUID.fromString(userId));

        List<ChannelResponse> updatedChannels = channels.stream()
                .map(channel -> {
                    List<MessageResponse> messages = chatServiceClient.getMessagesByChannel(channel.getId()).getBody();
                    List<ChannelParticipant> participants = channelParticipantRepository.findByChannelId(channel.getId());
                    return ChannelResponse.fromChannelAndMessage(channel, messages, participants);
                })
                .toList();

        ApiResponse<List<ChannelResponse>> response = ApiResponse.<List<ChannelResponse>>builder()
                .success(true)
                .message("Get All channels successfully")
                .data(updatedChannels)
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{channelId}/participants/ids")
    public ResponseEntity<List<UUID>> getParticipantIdsByChannelId(@PathVariable UUID channelId) {
        List<UUID> participantIds = channelService.getParticipantIdsByChannelId(channelId);
        return ResponseEntity.ok(participantIds);
    }


    @DeleteMapping("/{channelId}")
    public ResponseEntity<?> deleteChannel(@PathVariable UUID channelId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Channel has been deleted successfully")
                .success(true)
                .data(channelService.deleteChannel(channelId))
                .build());
    }

    @PostMapping("/{channelId}/participants")
    public ResponseEntity<ApiResponse<List<ChannelParticipant>>> addMembersToChannel(
            @PathVariable UUID channelId,
            @RequestBody AddMembersRequest request,
            @RequestHeader("X-Authenticated-User-Id") String addedByStr) {

        UUID addByUserId = UUID.fromString(addedByStr);

        List<ChannelParticipant> memberships = channelService.addMemberToChannel(channelId, request.getUserIds(), addByUserId);

        ApiResponse<List<ChannelParticipant>> response = ApiResponse.<List<ChannelParticipant>>builder()
                .success(true)
                .message("Users added to channel successfully")
                .data(memberships)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{channelId}/participants/leave")
    public ResponseEntity<ApiResponse<Boolean>> leaveChannel(
            @PathVariable UUID channelId,
            @RequestHeader("X-Authenticated-User-Id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        boolean result = channelService.leaveChannel(channelId, userId);

        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(result)
                .message(result ? "User has left the channel successfully" : "User is not a participant in this channel")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

}

