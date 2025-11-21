package com.nnson128.chatapps_base.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Generic event wrapper for all domain events
 * Provides a consistent structure across all event types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventWrapper<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private T payload;
}
