package com.example.hangmanproject.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Repository
public class WordRepository {

    private final Map<String, List<String>> words = new LinkedHashMap<>();
    private final Random random = new Random();

    public WordRepository() {
        words.put("Animals", List.of("ELEPHANT", "GIRAFFE", "DOLPHIN", "PENGUIN", "KANGAROO",
                "CROCODILE", "BUTTERFLY", "SQUIRREL", "OCTOPUS", "HEDGEHOG"));
        words.put("Cities", List.of("LONDON", "PARIS", "MADRID", "VIENNA", "BERLIN",
                "SOFIA", "PRAGUE", "ATHENS", "LISBON", "WARSAW"));
        words.put("Food", List.of("PIZZA", "SPAGHETTI", "CHOCOLATE", "PANCAKE", "SANDWICH",
                "LASAGNA", "BROCCOLI", "STRAWBERRY", "AVOCADO", "DUMPLING"));
    }

    public List<String> getCategories() {
        return new ArrayList<>(words.keySet());
    }

    public Optional<String> getRandomWord(String category) {
        List<String> list = words.get(category);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.get(random.nextInt(list.size())));
    }
}