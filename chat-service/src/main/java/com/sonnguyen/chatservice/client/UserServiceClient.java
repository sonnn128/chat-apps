package com.sonnguyen.chatservice.client;

import com.sonnguyen.chatservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/internal/{id}")
    UserResponse getUserById(@PathVariable UUID id);
}
