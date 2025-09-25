package com.sonnguyen.userservice.service;

import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.exception.CommonException;
import com.sonnguyen.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sonnguyen.userservice.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public UserResponse getUserProfile(UUID userId){
        return getUserById(userId);
    }

}