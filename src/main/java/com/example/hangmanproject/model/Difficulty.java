package com.example.hangmanproject.model;

public enum Difficulty {
    EASY(8, 1),
    MEDIUM(6, 2),
    HARD(4, 3);

    private final int maxMistakes;
    private final int multiplier;

    Difficulty(int maxMistakes, int multiplier) {
        this.maxMistakes = maxMistakes;
        this.multiplier = multiplier;
    }

    public int getMaxMistakes() { return maxMistakes; }
    public int getMultiplier() { return multiplier; }
}