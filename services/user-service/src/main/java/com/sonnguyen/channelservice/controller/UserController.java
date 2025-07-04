package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.dto.request.UserRegistrationRequest;
import com.sonnguyen.channelservice.dto.response.UserResponse;
import com.sonnguyen.channelservice.model.User;
import com.sonnguyen.channelservice.repository.UserRepository;
import com.sonnguyen.channelservice.service.UserService;
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        UserResponse createdUser = userService.registerUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmailForAuth(@RequestParam("email") String email) {
        User user = userService.findUserByEmailForAuth(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User user) {
        log.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@RequestHeader("X-Authenticated-User-Id") String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

}