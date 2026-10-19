package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.ReportResponse;
import com.example.hangmanproject.model.GameRecord;
import com.example.hangmanproject.repository.GameRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final GameRecordRepository gameRecordRepository;

    public ReportService(GameRecordRepository gameRecordRepository) {
        this.gameRecordRepository = gameRecordRepository;
    }

    public ReportResponse generateReport(int months, LocalDate anchorDate) {
        if (months != 1 && months != 6 && months != 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report period must be 1, 6, or 12 months.");
        }

        LocalDate endDay = (anchorDate != null) ? anchorDate : LocalDate.now();
        LocalDate startDay = endDay.minusMonths(months);

        LocalDateTime startDateTime = startDay.atStartOfDay();
        LocalDateTime endDateTime = endDay.atTime(LocalTime.MAX);

        List<GameRecord> records = gameRecordRepository.findByPlayedAtBetween(startDateTime, endDateTime);

        long totalEvents = records.size();
        long wonEvents = records.stream().filter(r -> "WON".equalsIgnoreCase(r.getResult())).count();
        long lostEvents = records.stream().filter(r -> "LOST".equalsIgnoreCase(r.getResult())).count();
        long totalPoints = records.stream().mapToLong(GameRecord::getPoints).sum();

        Map<String, Long> eventsByCategory = records.stream()
                .collect(Collectors.groupingBy(GameRecord::getCategory, Collectors.counting()));

        return new ReportResponse(
                months,
                startDay.format(DATE_FORMATTER),
                endDay.format(DATE_FORMATTER),
                totalEvents,
                wonEvents,
                lostEvents,
                totalPoints,
                eventsByCategory
        );
    }
}