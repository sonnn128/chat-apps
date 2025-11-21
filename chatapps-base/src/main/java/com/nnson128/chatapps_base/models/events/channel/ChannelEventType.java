package com.nnson128.chatapps_base.models.events.channel;

/**
 * Channel domain events
 */
public enum ChannelEventType {
    CHANNEL_CREATED("Channel created"),
    CHANNEL_UPDATED("Channel updated"),
    CHANNEL_DELETED("Channel deleted"),
    MEMBERS_ADDED_TO_CHANNEL("Members added to channel"),
    MEMBERS_REMOVED_FROM_CHANNEL("Members removed from channel"),
    CHANNEL_ARCHIVED("Channel archived"),
    CHANNEL_RESTORED("Channel restored");

    private final String description;

    ChannelEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
