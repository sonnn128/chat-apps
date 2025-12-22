package com.nnson128.chatservice.client;

import com.nnson128.chatapps_base.dto.res.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "relationship-service")
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

    @PostMapping("")
    ApiResponse<Map<String, Object>> createChannel(@RequestBody Map<String, Object> request);

    @PostMapping("/{channelId}/add-people")
    ApiResponse<Map<String, Object>> addPeopleToChannel(@PathVariable("channelId") UUID channelId,
                                                        @RequestBody Map<String, Object> request);

    @PostMapping("/direct")
    ApiResponse<Map<String, Object>> createDirectChannel(@RequestBody Map<String, Object> request);

}
