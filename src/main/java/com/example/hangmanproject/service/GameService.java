package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.GameResponse;
import com.example.hangmanproject.dto.LeaderboardEntry;
import com.example.hangmanproject.dto.StartGameRequest;
import com.example.hangmanproject.model.Difficulty;
import com.example.hangmanproject.model.Game;
import com.example.hangmanproject.model.GameRecord;          // NEW
import com.example.hangmanproject.model.GameStatus;
import com.example.hangmanproject.model.User;
import com.example.hangmanproject.repository.GameRecordRepository;   // NEW
import com.example.hangmanproject.repository.UserRepository;
import com.example.hangmanproject.repository.WordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;          // NEW
import java.time.temporal.ChronoUnit;    // NEW
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;   // NEW
    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();

    public GameService(WordRepository wordRepository,
                       UserRepository userRepository,
                       GameRecordRepository gameRecordRepository) {   // NEW
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
        this.gameRecordRepository = gameRecordRepository;   // NEW
    }

    public List<String> getCategories() {
        return wordRepository.getCategories();
    }

    public GameResponse startGame(StartGameRequest request) {
        Difficulty difficulty = parseDifficulty(request.difficulty());
        String word = wordRepository.getRandomWord(request.category())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category"));

        Game game = new Game(request.username().trim(), request.category(), word, difficulty);
        activeGames.put(game.getId(), game);
        return toResponse(game);
    }

    public GameResponse guess(String gameId, String rawLetter) {
        Game game = activeGames.get(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found. Start a new one.");
        }
        if (rawLetter == null || !rawLetter.matches("[A-Za-z]")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a single letter (A-Z)");
        }

        char letter = Character.toUpperCase(rawLetter.charAt(0));
        if (!game.getGuessedLetters().add(letter)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already tried " + letter);
        }

        if (game.getWord().indexOf(letter) < 0) {
            game.addMistake();
        }
        updateStatus(game);

        GameResponse response = toResponse(game);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            activeGames.remove(gameId);
        }
        return response;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return userRepository.findTop10ByOrderByScoreDesc().stream()
                .map(user -> new LeaderboardEntry(user.getUsername(), user.getScore()))
                .toList();
    }

    private void updateStatus(Game game) {
        boolean allFound = game.getWord().chars()
                .allMatch(c -> game.getGuessedLetters().contains((char) c));

        if (allFound) {
            Difficulty d = game.getDifficulty();
            int points = (d.getMaxMistakes() - game.getMistakes() + 1) * 10 * d.getMultiplier();
            game.win(points);
            saveScore(game.getUsername(), points);
            saveRecord(game);   // NEW
        } else if (game.getMistakes() >= game.getDifficulty().getMaxMistakes()) {
            game.lose();
            saveRecord(game);   // NEW
        }
    }

    // NEW: remembers who played, when, and how it ended
    private void saveRecord(Game game) {
        gameRecordRepository.save(new GameRecord(
                game.getUsername(),
                game.getCategory(),
                game.getDifficulty().name(),
                game.getStatus().name(),
                game.getPointsEarned(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
    }

    private void saveScore(String username, int points) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseGet(() -> {
            User created = new User();
            created.setUsername(username);
            return created;
        });
        user.setScore(user.getScore() + points);
        userRepository.save(user);
    }

    private Difficulty parseDifficulty(String value) {
        try {
            return Difficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown difficulty");
        }
    }

    private GameResponse toResponse(Game game) {
        StringBuilder masked = new StringBuilder();
        for (char c : game.getWord().toCharArray()) {
            masked.append(game.getGuessedLetters().contains(c) ? c : '_');
        }
        boolean over = game.getStatus() != GameStatus.IN_PROGRESS;

        return new GameResponse(
                game.getId(),
                game.getCategory(),
                game.getDifficulty().name(),
                masked.toString(),
                game.getGuessedLetters().stream().map(String::valueOf).toList(),
                game.getMistakes(),
                game.getDifficulty().getMaxMistakes(),
                game.getStatus().name(),
                game.getPointsEarned(),
                over ? game.getWord() : null);
    }
}