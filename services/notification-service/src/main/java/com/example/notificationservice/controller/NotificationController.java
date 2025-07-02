package com.example.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SimpMessageSendingOperations messagingTemplate;
    @PostMapping("/notify")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String message = payload.get("message");
        if (username == null || message == null) {
            return ResponseEntity.badRequest().body("Username and message are required.");
        }
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
        return ResponseEntity.ok("Notification sent to " + username);
    }
}
