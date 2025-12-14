package com.nnson128.relationshipservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(String topic, Object event) throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, eventJson);
    }

    public void sendMessage(String topic, String key, Object event) throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, key, eventJson);
    }
}
