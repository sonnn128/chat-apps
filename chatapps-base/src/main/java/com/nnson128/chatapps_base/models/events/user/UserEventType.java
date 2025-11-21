package com.nnson128.chatapps_base.models.events.user;

/**
 * User domain events
 */
public enum UserEventType {
    USER_REGISTERED("User registered"),
    USER_STATUS_CHANGED("User status changed"),
    USER_PROFILE_UPDATED("User profile updated"),
    USER_ONLINE("User online"),
    USER_OFFLINE("User offline"),
    USER_DELETED("User deleted");

    private final String description;

    UserEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
