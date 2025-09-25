package com.sonnguyen.channelservice.exception;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(String message) {
        super(message);
    }
    
    public ChannelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
