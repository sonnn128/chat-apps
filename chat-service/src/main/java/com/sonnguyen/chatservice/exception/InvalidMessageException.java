package com.sonnguyen.chatservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidMessageException extends ChatServiceException {
    public InvalidMessageException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_MESSAGE");
    }

    public InvalidMessageException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_MESSAGE", cause);
    }
}
