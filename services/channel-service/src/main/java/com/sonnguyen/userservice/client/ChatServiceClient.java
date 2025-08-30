package com.sonnguyen.userservice.client;

import com.sonnguyen.userservice.dto.request.SendMessageRequest;
import com.sonnguyen.userservice.dto.response.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "chat-service", path = "/api/v1/messages")
public interface ChatServiceClient {
    @GetMapping("/{channelId}")
    ResponseEntity<List<MessageResponse>> getMessagesByChannel(@PathVariable("channelId") UUID channelId);

    @PostMapping
    ResponseEntity<MessageResponse> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader("X-Authenticated-User-Id") String authenticatedUserId
    );

    @PostMapping("/save-only")
    ResponseEntity<MessageResponse> saveMessageOnly(
            @RequestBody SendMessageRequest request,
            @RequestHeader("X-Authenticated-User-Id") String authenticatedUserId
    );

}
