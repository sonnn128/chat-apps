package com.sonnguyen.channelservice.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error calling %s service: %s", serviceName, message));
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error calling %s service: %s", serviceName, message), cause);
    }
}
