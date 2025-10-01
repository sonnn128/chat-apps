package com.sonnguyen.chatservice.client;

import com.sonnguyen.chatservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "channel-service", url = "${channel-service.url}")
public interface ChannelServiceClient {
    @GetMapping("/api/v1/channels/{channelId}/participants/ids")
    ApiResponse<List<UUID>> getParticipantIdsByChannelId(@PathVariable("channelId") UUID channelId);

    @GetMapping("/api/v1/channels/{channelId}/participants/{userId}/check")
    ApiResponse<Boolean> checkUserIsParticipant(
                                 @PathVariable("channelId") UUID channelId,
                                 @PathVariable("userId") UUID userId
    );

    @GetMapping("/user/{userId}/ids")
    ApiResponse<List<UUID>> getChannelsByUserId(@PathVariable("userId") UUID userId);

}
