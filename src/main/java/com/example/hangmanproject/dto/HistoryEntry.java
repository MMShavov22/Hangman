package com.example.hangmanproject.dto;

public record HistoryEntry(
        String username,
        String category,
        String difficulty,
        String result,
        int points,
        String playedAt) {
}