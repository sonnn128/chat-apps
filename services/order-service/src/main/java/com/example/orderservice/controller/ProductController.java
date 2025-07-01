package com.example.orderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllProducts() {
        return ResponseEntity.ok(List.of(Map.of("id", "1", "name", "Laptop Dell (Public)"), Map.of("id", "2", "name", "Mouse Logitech (Public)")));
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> createProduct(@RequestBody Map<String, String> product) {
        log.warn("createProduct {}", product);
        return ResponseEntity.ok(Map.of("status", "Product '" + product.get("name") + "' created successfully by an ADMIN."));
    }
}
