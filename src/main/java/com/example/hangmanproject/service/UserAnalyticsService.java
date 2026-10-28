package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.UserRegistrationSummary;
import com.example.hangmanproject.model.User;
import com.example.hangmanproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserAnalyticsService {

    private final UserRepository userRepository;

    public UserAnalyticsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRegistrationSummary getRegistrationSummary() {
        LocalDateTime now = LocalDateTime.now();

        long today = countUsersBetween(now.toLocalDate().atStartOfDay(), now.toLocalDate().plusDays(1).atStartOfDay());
        long lastWeek = countUsersBetween(now.minusDays(7), now);
        long lastMonth = countUsersBetween(now.minusMonths(1), now);
        long lastYear = countUsersBetween(now.minusYears(1), now);

        List<RegistrationTrendPoint> trend = buildTrendForLastDays(30, now);
        String insight = generateInsight(today, lastWeek, lastMonth, lastYear);

        return new UserRegistrationSummary(today, lastWeek, lastMonth, lastYear, insight, trend);
    }

    private long countUsersBetween(LocalDateTime start, LocalDateTime end) {
        return userRepository.countByCreatedAtBetween(start, end);
    }

    private List<RegistrationTrendPoint> buildTrendForLastDays(int days, LocalDateTime end) {
        List<RegistrationTrendPoint> trend = new ArrayList<>();
        LocalDate startDate = end.toLocalDate().minusDays(days - 1);

        for (int i = 0; i < days; i++) {
            LocalDate day = startDate.plusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime finish = day.plusDays(1).atStartOfDay();
            long count = countUsersBetween(start, finish);
            trend.add(new RegistrationTrendPoint(day.getMonthValue() + "/" + day.getDayOfMonth(), count));
        }

        return trend;
    }

    private String generateInsight(long today, long lastWeek, long lastMonth, long lastYear) {
        if (today == 0 && lastWeek == 0 && lastMonth == 0 && lastYear == 0) {
            return "No registrations have been recorded yet. New accounts will appear here as soon as users sign up.";
        }

        long weeklyAverage = Math.max(1, lastWeek / 7);
        double uplift = (today > 0 && weeklyAverage > 0)
                ? ((double) today / weeklyAverage) * 100.0 - 100.0
                : 0.0;

        if (today >= weeklyAverage) {
            return String.format("Registration momentum is strong today with %d sign-ups, which is %.0f%% above the weekly average.", today, uplift);
        }

        if (today > 0) {
            return String.format("Current daily activity is %d sign-ups. The system suggests a moderate but stable growth trend overall.", today);
        }

        return String.format("The latest registration activity is low. The system remains healthy, but the last 30 days show %d sign-ups.", lastMonth);
    }
}
