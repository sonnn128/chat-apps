package com.sonnguyen.userservice.controller;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.request.UserRegistrationRequest;
import com.sonnguyen.userservice.dto.response.UserResponse;
import com.sonnguyen.userservice.model.User;
import com.sonnguyen.userservice.repository.UserRepository;
import com.sonnguyen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmailForAuth(@RequestParam("email") String email) {
        User user = userService.findUserByEmailForAuth(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User user) {
        log.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("Fetching user with id: {}", userId);
        return ResponseEntity.ok().body(ApiResponse.builder()
                        .message("User profile")
                        .success(true)
                        .data(userService.getUserProfile(UUID.fromString(userId)))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable UUID id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("User has been deleted: " + id)
                .build());
    }

}