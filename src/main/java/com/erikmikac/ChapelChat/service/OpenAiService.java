package com.erikmikac.ChapelChat.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    public String generateAnswer(String systemPrompt, String userMessage) {
        System.out.println(openAiApiKey);
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4",  // Or use "gpt-3.5-turbo" if preferred
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "temperature", 0.7
        );

        return webClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + openAiApiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            })
            .block(); // Blocking here for simplicity — async available if needed
    }
}
