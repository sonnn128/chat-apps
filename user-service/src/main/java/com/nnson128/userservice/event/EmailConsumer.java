package com.nnson128.userservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnson128.userservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "forgot-password-emails", groupId = "user-service-group")
    public void handleForgotPasswordEvent(String eventJson) {
        try {
            ForgotPasswordEvent event = objectMapper.readValue(eventJson, ForgotPasswordEvent.class);
            log.info("Received forgot password event for email: {}", event.getEmail());
            
            Context context = new Context();
            context.setVariable("name", event.getName());
            context.setVariable("resetLink", event.getResetLink());
            
            emailService.sendHtmlMessage(
                    event.getEmail(),
                    "Password Reset Request",
                    "password-reset",
                    context
            );
        } catch (JsonProcessingException e) {
            log.error("Error deserializing forgot password event", e);
        }
    }
}
