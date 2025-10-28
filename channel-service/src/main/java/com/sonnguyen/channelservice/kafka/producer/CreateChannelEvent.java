package com.sonnguyen.channelservice.kafka.producer;

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
public class CreateChannelEvent {
    private final String eventType = "NEW_CHANNEL";

    private UUID channelId;
    private String channelName; // Có thể là null nếu là chat 1-1
    private LocalDateTime createdAt;

    // creator user
    private UUID creatorId;
    private String creatorName;

    /**
     * Danh sách ID của tất cả các thành viên trong kênh (bao gồm cả người tạo).
     * notification-service sẽ dùng danh sách này để đẩy thông báo.
     */
    private List<UUID> memberIds;
}
