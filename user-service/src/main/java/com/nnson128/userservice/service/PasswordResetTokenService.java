package com.nnson128.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    // 15 minutes expiration
    private static final long EXPIRATION_MINUTES = 15;
    private static final String RESET_TOKEN_PREFIX = "RESET_PASSWORD:";

    public String createToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofMinutes(EXPIRATION_MINUTES));
        return token;
    }

    public UUID validateToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        Object userIdObj = redisTemplate.opsForValue().get(key);
        if (userIdObj == null) {
            return null;
        }
        return UUID.fromString((String) userIdObj);
    }

    public void deleteToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }
}
