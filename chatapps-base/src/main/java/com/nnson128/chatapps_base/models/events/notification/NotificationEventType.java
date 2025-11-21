package com.nnson128.chatapps_base.models.events.notification;

/**
 * Notification domain events
 */
public enum NotificationEventType {
    NOTIFICATION_CREATED("Notification created"),
    NOTIFICATION_SENT("Notification sent"),
    NOTIFICATION_READ("Notification read"),
    NOTIFICATION_DELETED("Notification deleted");

    private final String description;

    NotificationEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
