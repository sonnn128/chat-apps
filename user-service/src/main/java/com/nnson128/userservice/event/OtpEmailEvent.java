package com.nnson128.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpEmailEvent {
    private String email;
    private String name;
    private String otp;
    private String purpose; // "FORGOT_PASSWORD" or "REGISTRATION"
}
