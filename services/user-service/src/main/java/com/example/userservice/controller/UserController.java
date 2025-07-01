package com.example.userservice.controller;

import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // CREATE - Tạo mới một user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating new user with username: {}", user.getUsername());
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // READ - Lấy danh sách tất cả user
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // READ - Lấy thông tin một user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // UPDATE - Cập nhật thông tin user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        log.info("Updating user with id: {}", id);
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    // DELETE - Xóa một user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về status 204 No Content
    }
}