package com.sonnguyen.chatservice.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends ChatServiceException {
    public ExternalServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR");
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR", cause);
    }
}
