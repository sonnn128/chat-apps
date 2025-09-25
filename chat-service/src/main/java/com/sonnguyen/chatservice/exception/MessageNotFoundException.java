package com.sonnguyen.chatservice.exception;

import org.springframework.http.HttpStatus;

public class MessageNotFoundException extends ChatServiceException {
    public MessageNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND");
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", cause);
    }
}
