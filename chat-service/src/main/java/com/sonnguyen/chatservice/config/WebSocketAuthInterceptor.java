package com.sonnguyen.chatservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (command == StompCommand.CONNECT) {
                log.info("🔌 WebSocket: Client attempting to connect");
                log.info("✅ WebSocket: Connection allowed");
            } else if (command == StompCommand.SUBSCRIBE) {
                String destination = accessor.getDestination();
                log.info("📡 WebSocket: Client subscribing to: {}", destination);
            } else if (command == StompCommand.DISCONNECT) {
                log.info("🔌 WebSocket: Client disconnecting");
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (command == StompCommand.CONNECT && sent) {
                log.info("✅ WebSocket: Connection established successfully");
            } else if (command == StompCommand.SUBSCRIBE && sent) {
                log.info("✅ WebSocket: Subscription successful");
            }
        }
    }
}
