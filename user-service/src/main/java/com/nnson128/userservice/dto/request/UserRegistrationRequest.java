package com.nnson128.userservice.dto.request;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phone;
}
