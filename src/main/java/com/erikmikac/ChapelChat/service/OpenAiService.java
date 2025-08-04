package com.erikmikac.ChapelChat.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.erikmikac.ChapelChat.config.OpenAiProperties;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenAiService {

    private final WebClient webClient;
    private final OpenAiProperties openAiProperties;

    public OpenAiService(
            final WebClient.Builder webClientBuilder,
            final OpenAiProperties openAiProperties
            ) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.openAiProperties = openAiProperties;
    }

    public String generateAnswer(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", openAiProperties.getModel(), // Or use "gpt-3.5-turbo" if preferred
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)),
                "temperature", openAiProperties.getTemperature());

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openAiProperties.getKey())
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

    public Map<String, Object> analyzeMetadata(String userMessage) {
        String analysisPrompt = """
                You are an intent, tone, topic, and moderation analyzer for a church chatbot.

                Given a user's message, return:
                1. intent (e.g., ask_salvation, ask_prayer, ask_schedule, ask_doctrine, casual_greeting, challenge, unknown)
                2. tone (e.g., curious, joyful, confused, angry, skeptical, respectful, distressed)
                3. topic (e.g., salvation, baptism, grace, Trinity, service times, sin, heaven, hell, communion, unknown)
                4. flagged (true/false) — flag if message is profane, trolling, hostile, spammy, or emotionally concerning
                5. flagReason (optional) — reason it was flagged (e.g., "profanity", "trolling", "distress", "spam")

                Respond in this JSON format:
                {
                  "intent": "...",
                  "tone": "...",
                  "topic": "...",
                  "flagged": true,
                  "flagReason": "..."
                }

                Message: "%s"
                """
                .formatted(userMessage);
      
        try {
            String rawResponse = this.generateAnswer(analysisPrompt, userMessage); 
            ObjectMapper mapper = new ObjectMapper();
            final Map<String, Object> metadata = mapper.readValue(rawResponse, new TypeReference<>() {
            });
            metadata.put(ChatLogMetadataKey.OPENAI_MODEL.key(), openAiProperties.getModel());
            metadata.put(ChatLogMetadataKey.TEMPERATURE.key(), openAiProperties.getTemperature());
            return metadata;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(ChatLogMetadataKey.INTENT.key(), "unknown", ChatLogMetadataKey.TONE.key(), "unknown",
            ChatLogMetadataKey.OPENAI_MODEL.key(), openAiProperties.getModel(),
            ChatLogMetadataKey.TEMPERATURE.key(), openAiProperties.getTemperature() );
        }
    }

    @Async
    public CompletableFuture<Map<String, Object>> analyzeMetadataAsync(String userMessage) {
        return CompletableFuture.supplyAsync(() -> analyzeMetadata(userMessage));
    }
}
