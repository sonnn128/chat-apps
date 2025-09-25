package com.sonnguyen.chatservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "channel-service", path = "/api/v1/channels", configuration = com.sonnguyen.chatservice.config.FeignConfig.class)
public interface ChannelServiceClient {
    @GetMapping("/{channelId}/participants/ids")
    List<UUID> getParticipantIdsByChannelId(@PathVariable("channelId") UUID channelId);

    @GetMapping("/{channelId}/participants/{userId}/check")
    void checkUserIsParticipant( // Dùng void vì nó chỉ trả về status code
                                 @PathVariable("channelId") UUID channelId,
                                 @PathVariable("userId") UUID userId
    );

}
