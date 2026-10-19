package com.example.hangmanproject.repository;

import com.example.hangmanproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findTop10ByOrderByScoreDesc();

    List<User> findByActiveTrue();
}