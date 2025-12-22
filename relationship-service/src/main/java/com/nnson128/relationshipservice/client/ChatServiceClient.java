package com.nnson128.relationshipservice.client;

import com.nnson128.relationshipservice.dto.request.SendMessageRequest;
import com.nnson128.relationshipservice.dto.message.ChannelMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {

    @GetMapping("/api/v1/messages/{channelId}")
    List<ChannelMessageDto> getChannelMessages(@PathVariable UUID channelId);

    @PostMapping("/api/v1/messages/batch")
    Map<UUID, List<ChannelMessageDto>> getBatchChannelMessages(@RequestBody List<UUID> channelIds);

    @PostMapping("/api/v1/messages/internal")
    ChannelMessageDto sendMessage(
        @RequestParam("userId") UUID userId,
        @RequestBody SendMessageRequest request
    );
}
