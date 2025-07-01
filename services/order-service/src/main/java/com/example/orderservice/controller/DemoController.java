package com.example.orderservice.controller;

import com.example.orderservice.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/demo")
@RequiredArgsConstructor
@Slf4j
public class DemoController {

    private final ProductServiceClient productServiceClient;

    // Endpoint này yêu cầu người dùng phải là ADMIN
    @GetMapping("/call-admin-endpoint")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> demoCallAdminEndpoint() {
        log.info("Order Service is calling Product Service's admin endpoint...");
        try {
            // Feign client sẽ tự động truyền các header X-Authenticated-*
            String result = productServiceClient.callAdminEndpoint();
            log.info("Call to Product Service successful. Response: {}", result);
            return ResponseEntity.ok("Successfully called Product Service. It responded with: '" + result + "'");
        } catch (Exception e) {
            log.error("Call to Product Service failed.", e);
            return ResponseEntity.status(500).body("Failed to call Product Service: " + e.getMessage());
        }
    }

    // Endpoint này bất kỳ ai có token cũng có thể gọi
    @GetMapping("/call-public-endpoint")
    public ResponseEntity<String> demoCallPublicEndpoint() {
        log.info("Order Service is calling Product Service's public endpoint...");
        // API này của Product Service là public, nên lời gọi sẽ luôn thành công
        String result = productServiceClient.callAdminPublicEndpoint();
        return ResponseEntity.ok("Successfully called Product Service's public endpoint. It responded with: '" + result + "'");
    }
}
