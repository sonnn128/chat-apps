package com.nnson128.presenceservice.controller;

import com.nnson128.presenceservice.service.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Arrays;

@RestController
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    // Public API for clients via API Gateway
    @GetMapping("/api/presence/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestParam(name = "userIds") String userIds) {
        if (userIds == null || userIds.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid 'userIds' query parameter."));
        }
        List<String> ids = Arrays.stream(userIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<Map<String, Object>> data = presenceService.getStatusForUsers(ids);
        return ResponseEntity.ok(Map.of("data", data));
    }

    // Internal APIs (server-to-server) - called by chat-service
    @PostMapping("/internal/presence/connect")
    public ResponseEntity<Void> internalConnect(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null || userId.isEmpty()) return ResponseEntity.badRequest().build();
        presenceService.connect(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/presence/disconnect")
    public ResponseEntity<Void> internalDisconnect(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null || userId.isEmpty()) return ResponseEntity.badRequest().build();
        presenceService.disconnect(userId);
        return ResponseEntity.ok().build();
    }
}
