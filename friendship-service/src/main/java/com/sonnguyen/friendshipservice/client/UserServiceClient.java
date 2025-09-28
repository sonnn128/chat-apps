package com.sonnguyen.friendshipservice.client;

import com.sonnguyen.friendshipservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceClient {
    
    @GetMapping("/internal/{userId}")
    UserResponse getUserById(@PathVariable("userId") UUID userId);
    
    @GetMapping("/batch")
    List<UserResponse> getUsersByIds(@PathVariable("userIds") List<UUID> userIds);
}
