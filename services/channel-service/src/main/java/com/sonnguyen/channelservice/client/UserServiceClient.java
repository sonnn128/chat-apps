package com.sonnguyen.channelservice.client;

import com.sonnguyen.channelservice.dto.response.UserResponse; // DTO này cần được định nghĩa
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceClient {
    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable("id") UUID id);
}
