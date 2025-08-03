package com.erikmikac.ChapelChat.controller;

import com.erikmikac.ChapelChat.service.*;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.entity.ChurchApiKeyEntity;
import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskRequest;


@SpringBootTest
@AutoConfigureMockMvc
class AskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChurchApiKeyService apiKeyService;

    @MockBean
    private ChurchProfileService profileService;

    @MockBean
    private OpenAiService openAiService;

    @MockBean
    private ChatLogService chatLogService;

    @MockBean
    private AskRequest mockAskRequest;
    private final String VALID_API_KEY = "valid-api-key";
    private final String INVALID_API_KEY = "invalid-api-key";
    private final String CHURCH_ID = "hope-baptist";
    private final String USER_QUESTION = "What does it mean to be saved?";
    private final String BOT_ANSWER = "Salvation is by grace through faith.";
    private ChurchApiKeyEntity churchEntity = new ChurchApiKeyEntity();
    private Church church = new Church();

    @BeforeEach
    void setUp() {
        church.setName("Hope Baptist");
        church.setId(CHURCH_ID);
        churchEntity.setApiKey(VALID_API_KEY);
        churchEntity.setIsActive(true);
    }

    @Test
    void ask_withValidApiKey_returnsOk() throws Exception {
        UUID sessionId = UUID.randomUUID();
        AskRequest askRequest = new AskRequest(USER_QUESTION, sessionId);
        when(apiKeyService.getActiveChurchesByApiKey(VALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        when(apiKeyService.getChurchIdForValidKey(VALID_API_KEY)).thenReturn(Optional.of(CHURCH_ID));
        when(profileService.getSystemPromptFor(CHURCH_ID)).thenReturn("System prompt");
        when(openAiService.generateAnswer(any(), any())).thenReturn(BOT_ANSWER);
        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", VALID_API_KEY)
                .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(BOT_ANSWER));

        verify(chatLogService, times(1)).saveChatLogAndEnrich(any(ChatLog.class));
    }

    @Test
    void ask_withInvalidApiKey_returnsUnauthorized() throws Exception {
        UUID sessionId = UUID.randomUUID();
        AskRequest askRequest = new AskRequest(USER_QUESTION, sessionId);
        when(apiKeyService.getActiveChurchesByApiKey(INVALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        when(apiKeyService.getChurchIdForValidKey(INVALID_API_KEY)).thenReturn(Optional.empty());

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", INVALID_API_KEY)
                .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.response").value("Invalid or revoked API key."));

        verifyNoInteractions(profileService, openAiService, chatLogService);
    }

    @Test
    void ask_withMissingApiKey_returnsUnauthorized() throws Exception {
        UUID sessionId = UUID.randomUUID();
        AskRequest askRequest = new AskRequest(USER_QUESTION, sessionId);

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(apiKeyService, profileService, openAiService, chatLogService);
    }

    @Test
    void ask_withMissingMessage_returnsBadRequest() throws Exception {
        AskRequest askRequest = new AskRequest(null, UUID.randomUUID());
        when(apiKeyService.getActiveChurchesByApiKey(VALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        when(apiKeyService.getChurchIdForValidKey(VALID_API_KEY)).thenReturn(Optional.of(CHURCH_ID));

        mockMvc.perform(post("/ask")
                .header("X-Api-Key", VALID_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ask_withUnregisteredChurch_returnsBadRequest() throws Exception {
        AskRequest askRequest = new AskRequest(USER_QUESTION, UUID.randomUUID());
        when(apiKeyService.getActiveChurchesByApiKey(VALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        when(apiKeyService.getChurchIdForValidKey(VALID_API_KEY)).thenReturn(Optional.of(CHURCH_ID));
        when(profileService.getSystemPromptFor(CHURCH_ID)).thenThrow(new ChurchProfileNotFoundException(BOT_ANSWER));

        mockMvc.perform(post("/ask")
                .header("X-Api-Key", VALID_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(askRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response").value("That church's profile could not be found."));
    }
}
