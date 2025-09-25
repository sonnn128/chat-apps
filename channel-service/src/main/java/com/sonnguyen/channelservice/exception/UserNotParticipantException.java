package com.sonnguyen.channelservice.exception;

import java.util.UUID;

public class UserNotParticipantException extends RuntimeException {
    public UserNotParticipantException(UUID channelId, UUID userId) {
        super(String.format("User %s is not a participant in channel %s", userId, channelId));
    }
    
    public UserNotParticipantException(String message) {
        super(message);
    }
}
