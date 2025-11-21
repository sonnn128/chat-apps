package com.nnson128.chatapps_base.models.events.friendship;

/**
 * Friendship domain events
 */
public enum FriendshipEventType {
    FRIEND_REQUEST_SENT("Friend request sent"),
    FRIEND_REQUEST_ACCEPTED("Friend request accepted"),
    FRIEND_REQUEST_REJECTED("Friend request rejected"),
    FRIEND_REQUEST_CANCELLED("Friend request cancelled"),
    FRIEND_REMOVED("Friend removed"),
    FRIEND_BLOCKED("Friend blocked"),
    FRIEND_UNBLOCKED("Friend unblocked");

    private final String description;

    FriendshipEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
