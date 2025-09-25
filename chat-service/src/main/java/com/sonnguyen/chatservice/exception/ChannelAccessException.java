package com.sonnguyen.chatservice.exception;

import org.springframework.http.HttpStatus;

public class ChannelAccessException extends ChatServiceException {
    public ChannelAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "CHANNEL_ACCESS_DENIED");
    }

    public ChannelAccessException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, "CHANNEL_ACCESS_DENIED", cause);
    }
}
