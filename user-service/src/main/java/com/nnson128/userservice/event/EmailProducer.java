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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC_FORGOT_PASSWORD = "forgot-password-emails";
    private static final String TOPIC_OTP = "otp-emails";

    public void sendForgotPasswordEvent(ForgotPasswordEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Sending forgot password event for email: {}", event.getEmail());
            kafkaTemplate.send(TOPIC_FORGOT_PASSWORD, eventJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing forgot password event", e);
        }
    }

    public void sendOtpEmailEvent(OtpEmailEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Sending OTP email event for email: {} with purpose: {}", event.getEmail(), event.getPurpose());
            kafkaTemplate.send(TOPIC_OTP, eventJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing OTP email event", e);
        }
    }
}
