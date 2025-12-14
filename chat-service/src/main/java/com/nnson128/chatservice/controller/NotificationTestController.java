package com.nnson128.chatservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationTestController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/test-send")
    public ResponseEntity<?> testSend(
        @RequestParam UUID userId,
        @RequestParam(required = false) UUID channelId,
        @RequestParam(required = false) String content
    ) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "MESSAGE_SENT");
            Map<String, Object> key = new HashMap<>();
            key.put("channelId", channelId != null ? channelId : UUID.randomUUID());
            key.put("messageId", UUID.randomUUID());
            payload.put("key", key);
            payload.put("userId", userId);
            payload.put("content", content != null ? content : "Test message from NotificationTestController");
            payload.put("type", "NOTICE");
            payload.put("timestamp", Instant.now().toString());
            payload.put("senderName", "System");
            payload.put("senderAvatar", "");

            String destination = "/queue/notifications";
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
            return ResponseEntity.ok(Map.of("status", "sent", "userId", userId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
