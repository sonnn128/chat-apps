package com.example.notificationservice.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
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
