package com.sonnguyen.userservice.client;

import com.sonnguyen.userservice.dto.response.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "chat-service", path = "/api/v1/messages")
public interface ChatServiceClient {
    @GetMapping("/{channelId}")
    ResponseEntity<List<MessageResponse>> getMessagesByChannel(@PathVariable("channelId") UUID channelId);
}
