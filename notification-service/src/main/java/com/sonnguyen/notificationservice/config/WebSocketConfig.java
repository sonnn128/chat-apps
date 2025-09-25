package com.sonnguyen.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
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
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép tất cả các nguồn
                .setHandshakeHandler(new org.springframework.web.socket.server.support.DefaultHandshakeHandler())
                .withSockJS() // Hỗ trợ fallback cho các trình duyệt không hỗ trợ WebSocket
                .setHeartbeatTime(25000) // Heartbeat every 25 seconds
                .setDisconnectDelay(5000) // Disconnect delay 5 seconds
                .setStreamBytesLimit(128 * 1024) // 128KB stream limit
                .setHttpMessageCacheSize(1000); // Cache size for HTTP messages
    }

}