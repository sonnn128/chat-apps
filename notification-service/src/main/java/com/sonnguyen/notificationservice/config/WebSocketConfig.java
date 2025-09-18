package com.sonnguyen.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import lombok.RequiredArgsConstructor; // Thêm import

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor // Thêm annotation này
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" là tiền tố chung cho các broadcast messages
        // "/user" là tiền tố cho các user-specific messages
        config.enableSimpleBroker("/topic", "/user", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        // Quan trọng: Định nghĩa tiền tố cho các đích đến của người dùng.
        // Điều này cho phép SimpMessagingTemplate gửi đến "/user/{userId}/..."
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép tất cả các nguồn
                .withSockJS(); // Hỗ trợ fallback cho các trình duyệt không hỗ trợ WebSocket
    }

}