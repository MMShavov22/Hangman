package com.example.hangmanproject.dto;

import com.example.hangmanproject.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String username,
        String name,
        @Email String email,
        String phone,
        @NotNull Role role
) {}