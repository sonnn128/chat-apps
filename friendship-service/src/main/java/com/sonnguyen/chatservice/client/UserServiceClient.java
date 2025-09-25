package com.sonnguyen.chatservice.client;

import com.sonnguyen.chatservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/users", configuration = com.sonnguyen.chatservice.config.FeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable UUID id);
}
