package com.nnson128.chatapps_base.models.events.user.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.user.UserEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload for USER_STATUS_CHANGED event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusChangedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private UserEventType eventType;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("status")
    private String status; // ONLINE, OFFLINE, AWAY, DND

    @JsonProperty("changed_at")
    private LocalDateTime changedAt;
}
