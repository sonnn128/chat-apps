package com.nnson128.chatapps_base.models.events.notification.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.notification.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload for NOTIFICATION_SENT event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSentPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private NotificationEventType eventType;

    @JsonProperty("notification_id")
    private UUID notificationId;

    @JsonProperty("recipient_id")
    private UUID recipientId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("message")
    private String message;

    @JsonProperty("notification_type")
    private String notificationType; // MESSAGE, FRIEND_REQUEST, etc

    @JsonProperty("related_id")
    private String relatedId; // messageId, userId, channelId, etc

    @JsonProperty("sent_at")
    private LocalDateTime sentAt;
}
