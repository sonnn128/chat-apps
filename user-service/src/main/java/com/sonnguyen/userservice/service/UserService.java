package com.sonnguyen.userservice.service;

import com.sonnguyen.userservice.dto.request.UserRegistrationRequest;
import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.exception.CommonException;
import com.sonnguyen.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sonnguyen.userservice.model.Role;
import com.sonnguyen.userservice.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserRegistrationRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new CommonException("Email " + request.getEmail() + " is already in use.", HttpStatus.BAD_REQUEST);
        });
        userRepository.findByPhone(request.getPhone()).ifPresent(user -> {
            throw new CommonException("Phone " + request.getPhone() + " is already in use.", HttpStatus.BAD_REQUEST);
        });

        User newUser = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(newUser);
        return UserResponse.fromUser(savedUser);
    }
    public User findUserByEmailForAuth(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException("User not found with email: " + email, HttpStatus.NOT_FOUND));
    }

    public UserResponse getUserById(@PathVariable UUID id) {
        if(userRepository.findById(id).isPresent()) {
            return UserResponse.fromUser(userRepository.findById(id).get());
        }
        throw new CommonException("User with id " + id + " not found.", HttpStatus.NOT_FOUND);
    }

    public UserResponse updateUser(@PathVariable UUID id, @RequestBody User request) {
        if(userRepository.findById(id).isPresent()) {
            return UserResponse.fromUser(userRepository.findById(id).get());
        }
        throw new CommonException("User with id " + id + " not found.", HttpStatus.NOT_FOUND);
    }

    public UserResponse getUserProfile(String userId){
        return getUserById(UUID.fromString(userId));
    }

}