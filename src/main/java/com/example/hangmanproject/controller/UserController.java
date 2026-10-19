package com.example.hangmanproject.controller;

import com.example.hangmanproject.dto.CreateUserRequest;
import com.example.hangmanproject.dto.UpdateProfileRequest;
import com.example.hangmanproject.dto.UpdateRoleRequest;
import com.example.hangmanproject.model.User;
import com.example.hangmanproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    public List<User> getActiveUsers() {
        return userService.getAllActiveUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/profile")
    public User updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(id, request);
    }

    @PutMapping("/{id}/deactivate")
    public User deactivateUser(@PathVariable Long id) {
        return userService.deactivateUser(id);
    }

    @PutMapping("/{id}/role")
    public User updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @RequestHeader(value = "X-Caller-Role", required = false) String callerRole) {
        return userService.updateRole(id, request, callerRole);
    }
}