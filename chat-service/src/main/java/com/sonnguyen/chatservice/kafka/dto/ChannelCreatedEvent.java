package com.sonnguyen.chatservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreatedEvent {
    public static final String EVENT_TYPE = "CHANNEL_CREATED";

    // --- Thông tin về kênh mới ---
    private UUID channelId;
    private String channelName; // Có thể là null nếu là chat 1-1
    private LocalDateTime createdAt;

    // --- Thông tin về người tạo ---
    private UUID creatorId;
    private String creatorName;

    // --- Thông tin về người nhận ---
    /**
     * Danh sách ID của tất cả các thành viên trong kênh (bao gồm cả người tạo).
     * notification-service sẽ dùng danh sách này để đẩy thông báo.
     */
    private List<UUID> memberIds;
}
