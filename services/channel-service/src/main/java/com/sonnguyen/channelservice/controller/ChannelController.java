package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.service.ChannelService; // Sửa lại tên service
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    // Sửa tên biến cho đúng
    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannel(
            @RequestBody CreateChannelRequest request,
            @RequestHeader("X-Authenticated-User-Id") String creatorId) {

        // Gọi đúng phương thức từ ChannelService
        ChannelResponse newChannel = channelService.createChannel(request, UUID.fromString(creatorId));

        ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Channel created successfully")
                .data(newChannel)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint mới để chat-service kiểm tra quyền.
     * Endpoint này chỉ trả về status code, không cần body.
     */
    @GetMapping("/{channelId}/participants/{userId}/check")
    public ResponseEntity<Void> checkUserIsParticipant(
            @PathVariable UUID channelId,
            @PathVariable UUID userId) {

        boolean isParticipant = channelService.isUserParticipant(channelId, userId);
        return isParticipant
                ? ResponseEntity.ok().build() // 200 OK nếu là thành viên
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden nếu không phải
    }

    /**
     * Endpoint mới để lấy danh sách các kênh của người dùng hiện tại.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChannelResponse>>> getMyChannels(
            @RequestHeader("X-Authenticated-User-Id") String userId) {

        List<ChannelResponse> channels = channelService.getChannelsForUser(UUID.fromString(userId));

        ApiResponse<List<ChannelResponse>> response = ApiResponse.<List<ChannelResponse>>builder()
                .success(true)
                .data(channels)
                .build();
        return ResponseEntity.ok(response);
    }
}