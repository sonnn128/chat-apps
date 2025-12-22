package com.nnson128.relationshipservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelReadStatusService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String getRedisKey(UUID userId) {
        return "user:read_status:" + userId.toString();
    }

    public void markChannelAsRead(UUID userId, UUID channelId) {
        String key = getRedisKey(userId);
        long timestamp = Instant.now().toEpochMilli();
        redisTemplate.opsForHash().put(key, channelId.toString(), timestamp);
    }

    public Map<UUID, Long> getUserReadStatus(UUID userId) {
        String key = getRedisKey(userId);
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
        
        Map<UUID, Long> result = new HashMap<>();
        rawMap.forEach((k, v) -> {
            try {
                UUID channelId = UUID.fromString((String) k);
                Long timestamp = Long.valueOf(v.toString());
                result.put(channelId, timestamp);
            } catch (Exception e) {
                // Ignore invalid entries
            }
        });
        return result;
    }
    
    public Long getReadTimestamp(UUID userId, UUID channelId) {
        String key = getRedisKey(userId);
        Object val = redisTemplate.opsForHash().get(key, channelId.toString());
        if (val != null) {
            return Long.valueOf(val.toString());
        }
        return 0L;
    }
}
