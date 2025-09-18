package com.example.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonPropertyOrder({
        "success",
        "message"
})
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
