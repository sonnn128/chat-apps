package com.sonnguyen.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    @GetMapping
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String admin(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return "Hello Admin, you are authenticated and authorized!: " + userId;
    }

    @GetMapping("/public")
    public String adminPublic() {
        return "This is a public endpoint inside admin controller.";
    }
}