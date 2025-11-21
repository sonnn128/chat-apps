package com.nnson128.chatapps_base.models.events.message;

/**
 * Message domain events
 */
public enum MessageEventType {
    MESSAGE_SENT("Message sent successfully"),
    MESSAGE_DELIVERED("Message delivered to receiver"),
    MESSAGE_READ("Message read by receiver"),
    MESSAGE_EDITED("Message edited"),
    MESSAGE_DELETED("Message deleted"),
    MESSAGE_REACTED("Message reacted with emoji"),
    MESSAGE_UNREACTED("Message reaction removed"),
    MESSAGE_REPLIED("Message replied to"),
    MESSAGE_PINNED("Message pinned in conversation"),
    MESSAGE_UNPINNED("Message unpinned"),
    MESSAGE_FORWARDED("Message forwarded"),
    MESSAGE_STARRED("Message starred by user"),
    MESSAGE_UNSTARRED("Message unstarred");

    private final String description;

    MessageEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean shouldNotify() {
        return this == MESSAGE_SENT ||
                this == MESSAGE_REPLIED ||
                this == MESSAGE_REACTED;
    }

    public boolean isUrgent() {
        return this == MESSAGE_SENT ||
                this == MESSAGE_DELIVERED;
    }
}
