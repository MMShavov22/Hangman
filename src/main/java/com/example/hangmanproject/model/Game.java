package com.example.hangmanproject.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Game {

    private final String id = UUID.randomUUID().toString();
    private final String username;
    private final String category;
    private final String word;
    private final Difficulty difficulty;
    private final Set<Character> guessedLetters = new LinkedHashSet<>();
    private int mistakes;
    private int pointsEarned;
    private GameStatus status = GameStatus.IN_PROGRESS;

    public Game(String username, String category, String word, Difficulty difficulty) {
        this.username = username;
        this.category = category;
        this.word = word;
        this.difficulty = difficulty;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getCategory() { return category; }
    public String getWord() { return word; }
    public Difficulty getDifficulty() { return difficulty; }
    public Set<Character> getGuessedLetters() { return guessedLetters; }
    public int getMistakes() { return mistakes; }
    public int getPointsEarned() { return pointsEarned; }
    public GameStatus getStatus() { return status; }

    public void addMistake() { mistakes++; }
    public void win(int points) { status = GameStatus.WON; pointsEarned = points; }
    public void lose() { status = GameStatus.LOST; }
}