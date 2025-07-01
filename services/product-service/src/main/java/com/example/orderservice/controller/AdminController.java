package com.example.orderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products/admin")
public class AdminController {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String admin() {
        return "Hello Admin, you are authenticated and authorized!";
    }

    @GetMapping("/public")
    public String adminPublic() {
        return "This is a public endpoint inside admin controller.";
    }
}