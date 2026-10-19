package com.example.hangmanproject.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String category;
    private String difficulty;
    private String result;        // WON or LOST
    private int points;
    private LocalDateTime playedAt;

    protected GameRecord() {
        // required by JPA
    }

    public GameRecord(String username, String category, String difficulty,
                      String result, int points, LocalDateTime playedAt) {
        this.username = username;
        this.category = category;
        this.difficulty = difficulty;
        this.result = result;
        this.points = points;
        this.playedAt = playedAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public String getResult() { return result; }
    public int getPoints() { return points; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}