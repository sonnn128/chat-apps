package com.sonnguyen.channelservice.dto.request;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
}
