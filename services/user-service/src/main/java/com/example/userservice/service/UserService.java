package com.example.userservice.service;

import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Giả sử bạn dùng Spring Security
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserById(id);

        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFullName(userDetails.getFullName());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = getUserById(id);
        userRepository.delete(existingUser);
    }
}