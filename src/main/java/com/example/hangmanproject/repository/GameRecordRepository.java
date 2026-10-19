package com.example.hangmanproject.repository;

import com.example.hangmanproject.model.GameRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    List<GameRecord> findByPlayedAtBetween(LocalDateTime start, LocalDateTime end, Sort sort);

    List<GameRecord> findByPlayedAtBetween(LocalDateTime start, LocalDateTime end);
}