package com.sonnguyen.channelservice.client;

import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.dto.response.ChannelMessageDto;
import com.sonnguyen.channelservice.dto.response.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "chat-service", url = "${chat-service.url}")
public interface ChatServiceClient {

    @GetMapping("/api/v1/messages/{channelId}")
    List<Object> getChannelMessages(@PathVariable UUID channelId);

    @GetMapping("/api/v1/messages/user/{userId}/all")
    Map<UUID, List<ChannelMessageDto>> getAllMessagesByUserId(@PathVariable UUID userId);
}
