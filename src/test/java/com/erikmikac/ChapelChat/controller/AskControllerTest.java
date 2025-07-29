package com.erikmikac.ChapelChat.controller;


// File: AskControllerTest.java
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erikmikac.ChapelChat.config.SecurityConfig;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.service.ChurchApiKeyService;
import com.erikmikac.ChapelChat.service.ChurchProfileService;
import com.erikmikac.ChapelChat.service.OpenAiService;
@Import(SecurityConfig.class)
@WebMvcTest(AskController.class)
class AskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChurchApiKeyService apiKeyService;

    @MockBean
    private ChurchProfileService profileService;

    @MockBean
    private OpenAiService openAiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsAnswerWhenValidKeyAndMessage() throws Exception {
        String apiKey = "test-api-key";
        String churchId = "hope-baptist";
        String message = "Who is your pastor?";
        String prompt = "...system prompt...";
        String aiResponse = "Our pastor is Rev. John Matthews.";

        when(apiKeyService.getChurchIdForValidKey(apiKey)).thenReturn(Optional.of(churchId));
        when(profileService.getSystemPromptFor(churchId)).thenReturn(prompt);
        when(openAiService.generateAnswer(prompt, message)).thenReturn(aiResponse);

        AskRequest request = new AskRequest();
        request.setMessage(message);

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", apiKey)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(aiResponse));
    }

    @Test
    void returns401WhenApiKeyInvalid() throws Exception {
        when(apiKeyService.getChurchIdForValidKey("bad-key")).thenReturn(Optional.empty());

        AskRequest request = new AskRequest();
        request.setMessage("Hello?");

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", "bad-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
