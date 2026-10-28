package com.example.hangmanproject.dto;

import java.util.List;

public record UserRegistrationSummary(
        long today,
        long lastWeek,
        long lastMonth,
        long lastYear,
        String insight,
        List<RegistrationTrendPoint> trend
) {}

record RegistrationTrendPoint(
        String label,
        long count
) {}
