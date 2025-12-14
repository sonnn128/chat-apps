package com.nnson128.chatapps_base.constants;

public class KafkaTopics {
    // Pattern: <domain>.<entity>.<event-type>
    // Main topics
    // Event topics
    public static final String CHAT_MESSAGES = "chat.event.message";
    public static final String CHAT_TYPING_EVENTS = "chat.events.typing";
    public static final String CHAT_PRESENCE_EVENTS = "chat.events.presence";
    public static final String CHAT_READ_RECEIPTS = "chat.events.read";
    public static final String CHAT_NOTIFICATIONS = "chat.notifications";
    public static final String CHAT_CHANNEL_EVENTS = "chat.events.channel";
    public static final String FRIENDSHIP_EVENTS = "relationship.events.friendship";

    // User status events
    public static final String USER_STATUS_CHANGED = "user.events.status";

    // Legacy client connection events (kept for backwards compatibility)
    public static final String CLIENT_CONNECTED = "client.connected";
    public static final String CLIENT_DISCONNECTED = "client.disconnected";

    // Prevent instantiation
    private KafkaTopics() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}