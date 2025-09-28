package com.sonnguyen.channelservice.client;

import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.dto.message.ChannelMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "chat-service", url = "${chat-service.url}")
public interface ChatServiceClient {

    @GetMapping("/api/v1/messages/{channelId}")
    List<ChannelMessageDto> getChannelMessages(@PathVariable UUID channelId);

    @GetMapping("/api/v1/messages/user/{userId}/all")
    Map<UUID, List<ChannelMessageDto>> getAllMessagesByUserId(@PathVariable UUID userId);

    @PostMapping("/api/v1/messages/batch")
    Map<UUID, List<ChannelMessageDto>> getBatchChannelMessages(@RequestBody List<UUID> channelIds);

    @PostMapping("/api/v1/messages/{channelId}")
    ChannelMessageDto sendMessage(
            @PathVariable UUID channelId,
            @RequestParam("userId") UUID userId,
            @RequestBody SendMessageRequest request
    );
}
