package com.sonnguyen.channelservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnguyen.channelservice.dto.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestControlExceptionHandle {

    private final ObjectMapper objectMapper;


    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> handleFeignException(FeignException ex) {
        int status = ex.status();
        if (status < 100) { // Feign trả -1, không hợp lệ
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return ResponseEntity.status(status)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Feign error: " + ex.getMessage())
                        .build());
    }


    @ExceptionHandler({CommonException.class})
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> resolveCommonException(CommonException e) {
        log.error("CommonException caught: {}", e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.builder()
                        .message(e.getMessage())
                        .success(false)
                        .build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> resolveInvalidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("MethodArgumentNotValidException: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .message(errorMessage)
                .success(false)
                .build());
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> resolveAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .message("Authentication failed. " + e.getMessage())
                        .success(false)
                        .build()
        );
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> resolveAccessDeniedException(AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.builder()
                        .message("Access Denied: You do not have permission to perform this action.")
                        .success(false)
                        .build()
        );
    }
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> resolveGeneralException(Exception e) {
        log.error("An unexpected error occurred: ", e); // Log cả stack trace để debug
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.builder()
                        .message("An internal server error occurred. Please try again later.")
                        .success(false)
                        .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleJsonParseError(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Invalid JSON format: " + ex.getMostSpecificCause().getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Invalid parameter type: " + ex.getName())
                        .build()
        );
    }
}
