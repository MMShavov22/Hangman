package com.example.hangmanproject.dto;

import com.example.hangmanproject.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {}