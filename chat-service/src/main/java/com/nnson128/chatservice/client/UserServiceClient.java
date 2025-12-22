package com.nnson128.chatservice.client;

import com.nnson128.chatapps_base.dto.res.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/internal/{id}")
    UserResponse getUserById(@PathVariable UUID id);
}
