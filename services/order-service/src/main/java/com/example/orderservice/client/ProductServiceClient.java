package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service", configuration = com.example.orderservice.config.FeignClientConfiguration.class)
public interface ProductServiceClient {

//    like api need to call
    @GetMapping("/api/products/admin")
    String callAdminEndpoint();

    @GetMapping("/api/products/admin/public")
    String callAdminPublicEndpoint();
}
