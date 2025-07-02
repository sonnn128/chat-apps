package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannel(
            @RequestBody CreateChannelRequest request,
            @RequestHeader("X-Authenticated-User-Id") String creatorId
    ) {
        ChannelResponse newChannel = channelService.createChannel(request, UUID.fromString(creatorId));

        ApiResponse<ChannelResponse> response = ApiResponse.<ChannelResponse>builder()
                .success(true)
                .message("Channel created successfully")
                .data(newChannel)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
