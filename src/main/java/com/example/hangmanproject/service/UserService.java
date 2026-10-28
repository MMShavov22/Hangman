package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.UserRegistrationSummary;
import com.example.hangmanproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = new User(request.username(), request.name(), request.email(), request.phone(), request.role());
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.phone() != null) user.setPhone(request.phone());
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        return userRepository.save(user);
    }

    public User updateRole(Long userId, UpdateRoleRequest request, String callerRoleHeader) {
        validateAdminCaller(callerRoleHeader);
        User user = getUserById(userId);
        user.setRole(request.role());
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public UserRegistrationSummary getRegistrationSummary() {
        LocalDateTime now = LocalDateTime.now();
        long today = userRepository.countByCreatedAtBetween(now.toLocalDate().atStartOfDay(), now.toLocalDate().plusDays(1).atStartOfDay());
        long lastWeek = userRepository.countByCreatedAtBetween(now.minusDays(7), now);
        long lastMonth = userRepository.countByCreatedAtBetween(now.minusMonths(1), now);
        long lastYear = userRepository.countByCreatedAtBetween(now.minusYears(1), now);

        List<RegistrationTrendPoint> trend = new ArrayList<>();
        LocalDate start = now.toLocalDate().minusDays(29);
        for (int i = 0; i < 30; i++) {
            LocalDate date = start.plusDays(i);
            long count = userRepository.countByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            trend.add(new RegistrationTrendPoint(date.getMonthValue() + "/" + date.getDayOfMonth(), count));
        }

        String insight = generateInsight(today, lastWeek, lastMonth, lastYear);
        return new UserRegistrationSummary(today, lastWeek, lastMonth, lastYear, insight, trend);
    }

    private String generateInsight(long today, long lastWeek, long lastMonth, long lastYear) {
        if (today == 0 && lastWeek == 0 && lastMonth == 0 && lastYear == 0) {
            return "No registrations have been recorded yet. New users will appear here as soon as they sign up.";
        }

        long weeklyAverage = Math.max(1, lastWeek / 7);
        double uplift = (today > 0) ? ((double) today / weeklyAverage) * 100.0 - 100.0 : 0;
        if (today >= weeklyAverage) {
            return String.format("Registration momentum is high today with %d new users, which is %.0f%% above the weekly average.", today, uplift);
        }
        return String.format("The system shows %d sign-ups today and %d sign-ups in the last month, indicating a stable but moderate growth pattern.", today, lastMonth);
    }

    private void validateAdminCaller(String callerRoleHeader) {
        if (callerRoleHeader == null || !Role.ADMIN.name().equalsIgnoreCase(callerRoleHeader.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMINISTRATOR can manage roles");
        }
    }
}
