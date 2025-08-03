package com.erikmikac.ChapelChat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class InputSanitizationService {
    private final List<String> promptInjectionPhrases = List.of(
            "ignore previous", "disregard above", "you are now", "act as",
            "forget your instructions", "simulate jailbreak", "bypass filter",
            "as an ai model", "you can do anything", "change your identity");

    public boolean isInjectionAttempt(String input) {
        String lower = input.toLowerCase();
        return promptInjectionPhrases.stream().anyMatch(lower::contains);
    }

    public boolean isSafe(String input) {
        return !isInjectionAttempt(input);
    }

    public Optional<String> getFlaggedPhrase(String input) {
        String lower = input.toLowerCase();
        return promptInjectionPhrases.stream()
                .filter(lower::contains)
                .findFirst();
    }
}
