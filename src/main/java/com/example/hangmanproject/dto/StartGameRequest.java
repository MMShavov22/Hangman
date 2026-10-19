package com.example.hangmanproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartGameRequest(
        @NotBlank @Size(max = 20) String username,
        @NotBlank String category,
        @NotBlank String difficulty) {
}