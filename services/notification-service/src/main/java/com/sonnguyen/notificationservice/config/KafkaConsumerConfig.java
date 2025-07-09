//package com.sonnguyen.notificationservice.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.listener.CommonErrorHandler;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.kafka.support.serializer.DeserializationException;
//import org.springframework.util.backoff.FixedBackOff;
//
//@Configuration
//@Slf4j
//public class KafkaConsumerConfig {
//
//    /**
//     * Error Handler để xử lý các lỗi, đặc biệt là lỗi deserialization.
//     */
//    @Bean
//    public CommonErrorHandler commonErrorHandler() {
//        DefaultErrorHandler handler = new DefaultErrorHandler((consumerRecord, exception) -> {
//            log.error("--- KAFKA PROCESSING ERROR ---");
//            log.error("Failed to process message from topic: {}", consumerRecord.topic());
//            log.error("Key: {}, Partition: {}, Offset: {}",
//                    consumerRecord.key(), consumerRecord.partition(), consumerRecord.offset());
//            log.error("Exception: {}", exception.getMessage());
//            // Có thể thêm logic gửi vào Dead Letter Topic ở đây
//        }, new FixedBackOff(1000L, 2L)); // Thử lại 2 lần, cách nhau 1s
//
//        // Không thử lại các lỗi Deserialization
//        handler.addNotRetryableExceptions(DeserializationException.class);
//
//        return handler;
//    }
//
//    /**
//     * Tạo ra một Listener Container Factory hoàn toàn mới.
//     * Đây là cách làm đúng để tránh Circular Dependency.
//     *
//     * @param configurer - Một helper do Spring Boot cung cấp để áp dụng các cấu hình từ application.properties.
//     * @param consumerFactory - Factory để tạo ra các consumer, cũng do Spring Boot cung cấp.
//     * @param commonErrorHandler - Error Handler chúng ta đã định nghĩa ở trên.
//     * @return Một ContainerFactory đã được cấu hình hoàn chỉnh.
//     */
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
//            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
//            ConsumerFactory<Object, Object> consumerFactory,
//            CommonErrorHandler commonErrorHandler) {
//
//        // 1. Tạo một factory mới
//        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//
//        // 2. Sử dụng configurer để áp dụng các thuộc tính từ application.properties
//        //    (ví dụ: bootstrap-servers, group-id, deserializers,...) vào factory mới này.
//        configurer.configure(factory, consumerFactory);
//
//        // 3. Gắn Error Handler tùy chỉnh của chúng ta vào factory
//        factory.setCommonErrorHandler(commonErrorHandler);
//
//        // 4. Trả về factory đã được cấu hình
//        return factory;
//    }
//}