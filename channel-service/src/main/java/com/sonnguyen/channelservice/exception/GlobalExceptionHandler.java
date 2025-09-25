package com.sonnguyen.channelservice.exception;

import com.sonnguyen.channelservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleChannelNotFoundException(ChannelNotFoundException ex) {
        log.error("ChannelNotFoundException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("Channel not found: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserNotParticipantException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotParticipantException(UserNotParticipantException ex) {
        log.error("UserNotParticipantException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("Access denied: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleExternalServiceException(ExternalServiceException ex) {
        log.error("ExternalServiceException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("External service error: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("Invalid request: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("Internal server error: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.error("HttpClientErrorException: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("External service error: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("HttpServerErrorException: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("External service unavailable: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("ResourceAccessException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("Service temporarily unavailable: " + ex.getMessage())
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("An unexpected error occurred")
                .data(null)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
