package com.nnson128.userservice.service;

import com.nnson128.chatapps_base.exception.CommonException;
import com.nnson128.userservice.dto.response.UserResponse;
import com.nnson128.userservice.repository.UserRepository;
import com.nnson128.userservice.util.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nnson128.userservice.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

    private final UserRepository userRepository;

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

    public UserResponse updateUser(UUID id, User request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + id, HttpStatus.NOT_FOUND));
        
        if (request.getFirstname() != null) existingUser.setFirstname(request.getFirstname());
        if (request.getLastname() != null) existingUser.setLastname(request.getLastname());
        if (request.getPhone() != null) existingUser.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) existingUser.setAvatarUrl(request.getAvatarUrl());
        
        return UserResponse.fromUser(userRepository.save(existingUser));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new CommonException(USER_NOT_FOUND_MESSAGE + id, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    public UserResponse getUserProfile(UUID userId){
        return getUserById(userId);
    }



    public UserResponse updateUserAvatarLegacy(UUID userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));
        
        // For now, just return the existing user without actually processing the avatar
        // In a real implementation, you would save the file and update the avatar URL
        return UserResponse.fromUser(existingUser);
    }

    public UserResponse searchUserByPhone(String phone) {
        // Normalize the search phone number
        String normalizedPhone = PhoneNumberUtils.normalizeVietnamesePhone(phone);
        
        User user = userRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new CommonException("User not found with phone: " + phone, HttpStatus.NOT_FOUND));
        return UserResponse.fromUser(user);
    }

    public void updateUserAvatar(UUID userId, String avatarUrl, String avatarPublicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));
        
        // Delete old avatar if exists and different from new one
        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().equals(avatarPublicId)) {
            // Note: In production, you might want to delete the old file from media service
            // This requires calling media service to delete the old file
        }
        
        user.setAvatarUrl(avatarUrl);
        user.setAvatarPublicId(avatarPublicId);
        userRepository.save(user);
    }

    public String getUserAvatarUrl(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));
        return user.getAvatarUrl();
    }

    public String getUserAvatarPublicId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND_MESSAGE + userId, HttpStatus.NOT_FOUND));
        return user.getAvatarPublicId();
    }

}