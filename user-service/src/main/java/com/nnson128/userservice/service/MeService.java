package com.nnson128.userservice.service;

import com.nnson128.chatapps_base.exception.CommonException;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.model.User;
import com.nnson128.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";
    private final UserRepository userRepository;

    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));
        return UserResponse.fromUser(user);
    }

    public UserResponse updateUserProfile(UUID userId, User updateRequest) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));

        // Only update firstname and lastname
        if (updateRequest.getFirstname() != null) {
            existingUser.setFirstname(updateRequest.getFirstname());
        }
        if (updateRequest.getLastname() != null) {
            existingUser.setLastname(updateRequest.getLastname());
        }

        User savedUser = userRepository.save(existingUser);
        return UserResponse.fromUser(savedUser);
    }
}
