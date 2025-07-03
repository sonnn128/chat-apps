package com.sonnguyen.notificationservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageSentEvent {
    /**
     * Một trường để client có thể dễ dàng phân biệt các loại sự kiện.
     * Ví dụ: if (event.eventType === 'NEW_MESSAGE') { ... }
     */
    private final String eventType = "NEW_MESSAGE";

    // --- Thông tin về chính tin nhắn ---
    private UUID messageId;
    private UUID channelId;
    private String content;
    private Date timestamp;

    // --- Thông tin về người gửi ---
    private UUID senderId;
    private String senderName;  // Rất hữu ích, giúp client không cần gọi thêm API để lấy tên người gửi
    private String senderAvatar; // Tùy chọn, nếu bạn muốn hiển thị avatar ngay lập tức

    // --- Thông tin về người nhận ---
    /**
     * Danh sách ID của tất cả các thành viên trong kênh sẽ nhận được thông báo này.
     * Đây là trường quan trọng nhất để notification-service hoạt động.
     */
    private List<UUID> recipientIds;
}
