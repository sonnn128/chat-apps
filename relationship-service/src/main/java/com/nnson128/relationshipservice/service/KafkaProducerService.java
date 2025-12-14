package com.nnson128.relationshipservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        try {
            Message<String> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();

            kafkaTemplate.send(kafkaMessage).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.error("Message sent successfully to topic: {} with message: {}", topic, message);
                } else {
                    log.error("Message sent successfully to topic: {} with message: {}", topic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Message sent successfully to topic: {} with message: {}", topic, message);
        }
    }

    public void sendMessageWithKey(String topic, String key, String message) {
        try {
            kafkaTemplate.send(topic, key, message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent successfully to topic: {} with message: {}", topic, message);
                } else {
                    log.error("Error sending message to topic: {}", topic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Error in sendMessageWithKey: ", e);
        }
    }
}

