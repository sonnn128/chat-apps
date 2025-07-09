package com.sonnguyen.notificationservice.events;

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
public class NewChannelCreatedEvent {
    private final String eventType = "NEW_CHANNEL";

    private UUID channelId;
    private String channelName; // Có thể là null nếu là chat 1-1
    private LocalDateTime createdAt;

    private UUID creatorId;
    private String creatorName;

    private List<UUID> memberIds; // Đổi tên từ recipientIds cho rõ nghĩa hơn trong ngữ cảnh này
}


