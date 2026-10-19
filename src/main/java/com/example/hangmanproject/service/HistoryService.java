package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.HistoryEntry;
import com.example.hangmanproject.repository.GameRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HistoryService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GameRecordRepository gameRecordRepository;

    public HistoryService(GameRecordRepository gameRecordRepository) {
        this.gameRecordRepository = gameRecordRepository;
    }

    // Who played between two dates (inclusive), sorted by date, hour and minute
    public List<HistoryEntry> getHistory(LocalDate from, LocalDate to, String order) {
        LocalDate start = (from != null) ? from : LocalDate.of(2000, 1, 1);
        LocalDate end = (to != null) ? to : LocalDate.of(2999, 12, 31);
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }

        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(order)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(order)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order must be 'asc' or 'desc'");
        }

        return gameRecordRepository
                .findByPlayedAtBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX),
                        Sort.by(direction, "playedAt"))
                .stream()
                .map(r -> new HistoryEntry(r.getUsername(), r.getCategory(), r.getDifficulty(),
                        r.getResult(), r.getPoints(), r.getPlayedAt().format(DISPLAY)))
                .toList();
    }
}