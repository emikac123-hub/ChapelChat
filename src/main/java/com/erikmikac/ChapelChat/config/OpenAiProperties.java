package com.erikmikac.ChapelChat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "openai.api")
@Data
public class OpenAiProperties {

    /**
     * Your OpenAI secret API key (stored securely via env var or K8s Secret).
     */
    private String key;

    /**
     * The model to use for generation (e.g., gpt-4o, gpt-3.5-turbo).
     */
    private String model;

    /**
     * The temperature setting for generation (0.0 = deterministic, 1.0 = creative).
     */
    private Double temperature;

    private String baseUrl;
}
