package com.example.hangmanproject.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
        String name,
        @Email String email,
        String phone
) {}