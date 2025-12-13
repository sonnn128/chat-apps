package com.nnson128.chatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.chatapps_base.constants.KafkaTopics;
import com.nnson128.chatapps_base.models.events.EventWrapper;
import com.nnson128.chatapps_base.models.events.call.payloads.CallSignalPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/signal")
    public ResponseEntity<Void> sendSignal(@RequestBody CallSignalPayload payload, @AuthenticationPrincipal Jwt jwt) {
        try {
            UUID senderId = UUID.fromString(jwt.getSubject());
            payload.setSenderId(senderId);

            EventWrapper<CallSignalPayload> event = EventWrapper.<CallSignalPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CALL_SIGNAL")
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .build();

            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.CHAT_NOTIFICATIONS, jsonEvent);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
