package com.sonnguyen.chatservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatServiceException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;

    public ChatServiceException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public ChatServiceException(String message, HttpStatus httpStatus, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
