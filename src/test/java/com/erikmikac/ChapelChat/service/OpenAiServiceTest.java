package com.erikmikac.ChapelChat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.erikmikac.ChapelChat.config.OpenAiProperties;

import reactor.core.publisher.Mono;

public class OpenAiServiceTest {

    @Test
    void testGenerateAnswer_returnsExpectedContent() {
        // Arrange
        OpenAiProperties props = new OpenAiProperties();
        props.setKey("test-key");
        props.setModel("gpt-test-model");
        props.setTemperature(0.5);

        WebClient client = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse
                                .create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body("{\"choices\":[{\"message\":{\"content\":\"This is a test response\"}}]}")
                                .build()))
                .build();

        OpenAiService service = new OpenAiService(client.mutate(), props);

        // Act
        String result = service.generateAnswer("System prompt", "User message");

        // Assert
        assertEquals("This is a test response", result);
    }
}
