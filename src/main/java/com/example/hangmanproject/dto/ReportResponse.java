package com.example.hangmanproject.dto;

import java.util.Map;

public record ReportResponse(
        int periodMonths,
        String startDate,
        String endDate,
        long totalEvents,
        long wonEvents,
        long lostEvents,
        long totalPointsEarned,
        Map<String, Long> eventsByCategory
) {
}