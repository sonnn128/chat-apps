package com.nnson128.relationshipservice.client;

import com.nnson128.chatapps_base.dto.res.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/internal/{userId}")
    UserResponse getUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/api/v1/users/batch")
    List<UserResponse> getUsersByIds(@PathVariable("userIds") List<UUID> userIds);
}
