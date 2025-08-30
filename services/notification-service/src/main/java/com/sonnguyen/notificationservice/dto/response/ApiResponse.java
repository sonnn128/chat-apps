package com.sonnguyen.notificationservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
