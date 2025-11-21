package com.nnson128.presenceservice.service;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "presence:"; // presence:{userId} -> hash {status, lastSeen}
    private static final Duration TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    public PresenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void connect(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        String now = Instant.now().toString();
        redisTemplate.opsForHash().put(key, "status", "online");
        redisTemplate.opsForHash().put(key, "lastSeen", now);
        redisTemplate.expire(key, TTL);
    }

    public void disconnect(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        String now = Instant.now().toString();
        redisTemplate.opsForHash().put(key, "status", "offline");
        redisTemplate.opsForHash().put(key, "lastSeen", now);
        // remove TTL / persist key so it stays (we'll persist using low-level connection)
        try {
            final byte[] keyBytes = redisTemplate.getStringSerializer().serialize(key);
            redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.persist(keyBytes));
        } catch (Exception ignored) {
            // best-effort
        }
    }

    public List<Map<String, Object>> getStatusForUsers(List<String> userIds) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String userId : userIds) {
            String key = PRESENCE_KEY_PREFIX + userId;
            Object statusObj = redisTemplate.opsForHash().get(key, "status");
            Object lastSeenObj = redisTemplate.opsForHash().get(key, "lastSeen");
            String status = statusObj == null ? "offline" : statusObj.toString();
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", userId);
            entry.put("status", status);
            if ("offline" .equals(status) && lastSeenObj != null) {
                entry.put("lastSeen", lastSeenObj.toString());
            }
            out.add(entry);
        }
        return out;
    }
}
