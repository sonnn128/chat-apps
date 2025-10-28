package com.sonnguyen.chatservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import com.sonnguyen.chatservice.client.PresenceServiceClient;
import com.sonnguyen.chatservice.kafka.producer.PresenceEventPublisher;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final PresenceServiceClient presenceClient;
    private final PresenceEventPublisher presencePublisher;
    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(PresenceServiceClient presenceClient, PresenceEventPublisher presencePublisher, JwtDecoder jwtDecoder) {
        this.presenceClient = presenceClient;
        this.presencePublisher = presencePublisher;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (command == StompCommand.CONNECT) {
                log.info("🔌 WebSocket: Client attempting to connect");
                String userId = extractUserId(accessor);
                if (userId != null) {
                    try {
                        // publish to Kafka (preferred)
                        presencePublisher.publishConnect(userId);
                    } catch (Exception ex) {
                        log.warn("Failed to publish presence connect for {}: {}", userId, ex.getMessage());
                    }
                    try {
                        // fallback HTTP call
                        presenceClient.connect(Map.of("userId", userId));
                        log.info("Presence: POST /internal/presence/connect for {}", userId);
                    } catch (Exception ex) {
                        log.warn("Failed to notify presence-service connect via HTTP for {}: {}", userId, ex.getMessage());
                    }
                }
                log.info("✅ WebSocket: Connection allowed");
            } else if (command == StompCommand.SUBSCRIBE) {
                String destination = accessor.getDestination();
                log.info("📡 WebSocket: Client subscribing to: {}", destination);
            } else if (command == StompCommand.DISCONNECT) {
                log.info("🔌 WebSocket: Client disconnecting");
                String userId = extractUserId(accessor);
                if (userId != null) {
                    try {
                        presencePublisher.publishDisconnect(userId);
                    } catch (Exception ex) {
                        log.warn("Failed to publish presence disconnect for {}: {}", userId, ex.getMessage());
                    }
                    try {
                        presenceClient.disconnect(Map.of("userId", userId));
                        log.info("Presence: POST /internal/presence/disconnect for {}", userId);
                    } catch (Exception ex) {
                        log.warn("Failed to notify presence-service disconnect via HTTP for {}: {}", userId, ex.getMessage());
                    }
                }
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

    private String extractUserId(StompHeaderAccessor accessor) {
        // try Authorization header (Bearer token)
        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                if (jwt != null && jwt.getSubject() != null) return jwt.getSubject();
            } catch (Exception ignored) {
            }
        }
        // try native header X-User-Id for backwards compatibility
        String userId = accessor.getFirstNativeHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) return userId;
        // try principal
        if (accessor.getUser() != null) {
            try {
                return accessor.getUser().getName();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
