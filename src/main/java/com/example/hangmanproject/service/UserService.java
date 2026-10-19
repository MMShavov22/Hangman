package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.CreateUserRequest;
import com.example.hangmanproject.dto.UpdateProfileRequest;
import com.example.hangmanproject.dto.UpdateRoleRequest;
import com.example.hangmanproject.model.Role;
import com.example.hangmanproject.model.User;
import com.example.hangmanproject.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = new User(request.username(), request.name(), request.email(), request.phone(), request.role());
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.phone() != null) user.setPhone(request.phone());
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        return userRepository.save(user);
    }

    public User updateRole(Long userId, UpdateRoleRequest request, String callerRoleHeader) {
        validateAdminCaller(callerRoleHeader);
        User user = getUserById(userId);
        user.setRole(request.role());
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    private void validateAdminCaller(String callerRoleHeader) {
        if (callerRoleHeader == null || !Role.ADMIN.name().equalsIgnoreCase(callerRoleHeader.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMINISTRATOR can manage roles");
        }
    }
}