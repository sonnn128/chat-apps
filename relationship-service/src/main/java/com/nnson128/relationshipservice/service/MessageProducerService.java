package com.nnson128.relationshipservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for producing and sending events to Kafka topics as JSON strings.
 * All events are serialized to JSON strings before sending to Kafka.
 */
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send event to Kafka topic as JSON string
     * @param topic Kafka topic name
     * @param event Event object to serialize and send
     */
    public void sendMessage(String topic, Object event) throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, eventJson);
    }

    /**
     * Send event to Kafka topic with a key as JSON string
     * @param topic Kafka topic name
     * @param key Message key for Kafka partitioning
     * @param event Event object to serialize and send
     */
    public void sendMessage(String topic, String key, Object event) throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, key, eventJson);
    }
}
