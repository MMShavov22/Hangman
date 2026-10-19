package com.example.hangmanproject.dto;

import java.util.List;

public record GameResponse(
        String gameId,
        String category,
        String difficulty,
        String maskedWord,
        List<String> guessedLetters,
        int mistakes,
        int maxMistakes,
        String status,
        int pointsEarned,
        String word) {   // only filled in when the game is over
}