package com.example.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" là tiền tố cho các "chủ đề" mà client có thể đăng ký (subscribe) để nhận tin.
        // Server sẽ gửi tin nhắn đến các đích bắt đầu bằng /topic.
        config.enableSimpleBroker("/topic", "/user");

        // "/app" là tiền tố cho các đích mà client gửi tin nhắn đến server.
        // Ví dụ: client gửi đến /app/hello, nó sẽ được route đến một method @MessageMapping("/hello").
        config.setApplicationDestinationPrefixes("/app");
//        config.setUserDestinationPrefix("/user");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
