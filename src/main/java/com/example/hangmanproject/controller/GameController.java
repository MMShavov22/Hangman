package com.example.hangmanproject.controller;

import com.example.hangmanproject.dto.GameResponse;
import com.example.hangmanproject.dto.GuessRequest;
import com.example.hangmanproject.dto.LeaderboardEntry;
import com.example.hangmanproject.dto.StartGameRequest;
import com.example.hangmanproject.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return gameService.getCategories();
    }

    @PostMapping("/game/start")
    public GameResponse start(@Valid @RequestBody StartGameRequest request) {
        return gameService.startGame(request);
    }

    @PostMapping("/game/{id}/guess")
    public GameResponse guess(@PathVariable String id, @RequestBody GuessRequest request) {
        return gameService.guess(id, request.letter());
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard() {
        return gameService.getLeaderboard();
    }
}