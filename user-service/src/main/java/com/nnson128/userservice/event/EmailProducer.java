package com.nnson128.userservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducer {

    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "forgot-password-emails";

    public void sendForgotPasswordEvent(ForgotPasswordEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Sending forgot password event for email: {}", event.getEmail());
            kafkaTemplate.send(TOPIC, eventJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing forgot password event", e);
        }
    }
}
