package com.nnson128.chatservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${notification-service.url:http://notification-service:9006}")
public interface PresenceServiceClient {

    @PostMapping("/internal/presence/connect")
    void connect(@RequestBody Map<String, String> body);

    @PostMapping("/internal/presence/disconnect")
    void disconnect(@RequestBody Map<String, String> body);
}
