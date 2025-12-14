package com.nnson128.chatservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import com.nnson128.chatservice.client.PresenceServiceClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final PresenceServiceClient presenceClient;
    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (command == StompCommand.CONNECT) {
                String userId = extractUserId(accessor);
                if (userId != null) {
                    try {
                        // HTTP call to presence service
                        presenceClient.connect(Map.of("userId", userId));
                    } catch (Exception ex) {
                    }
                }
            } else if (command == StompCommand.SUBSCRIBE) {
                String destination = accessor.getDestination();
            } else if (command == StompCommand.DISCONNECT) {
                String userId = extractUserId(accessor);
                if (userId != null) {
                    try {
                        presenceClient.disconnect(Map.of("userId", userId));
                    } catch (Exception ex) {
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
            } else if (command == StompCommand.SUBSCRIBE && sent) {
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
