package com.example.userservice.client;

import com.example.userservice.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service", configuration = FeignClientConfiguration.class)
public interface ProductServiceClient {

//    like api need to call
    @GetMapping("/api/products/admin")
    String callAdminEndpoint();

    @GetMapping("/api/products/admin/public")
    String callAdminPublicEndpoint();
}
