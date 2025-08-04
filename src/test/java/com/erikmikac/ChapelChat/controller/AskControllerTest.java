package com.erikmikac.ChapelChat.controller;

import java.util.Optional;

import com.erikmikac.ChapelChat.config.SecurityConfig;
import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.entity.ChurchApiKeyEntity;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.AskService;
import com.erikmikac.ChapelChat.service.ChatLogService;
import com.erikmikac.ChapelChat.service.ChurchApiKeyService;
import com.erikmikac.ChapelChat.service.ChurchProfileService;
import com.erikmikac.ChapelChat.service.OpenAiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.erikmikac.ChapelChat.repository.AppUserRepository;

@WebMvcTest(AskController.class)
@Import(SecurityConfig.class)
public class AskControllerTest {

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
    private AppUserRepository AppUserRepository;
    @MockBean
    AskService askService;
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

    void ask_withValidRequest_returnsResponse() throws Exception {
        // Arrange
        when(apiKeyService.getActiveChurchesByApiKey(VALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        when(openAiService.generateAnswer(any(), any())).thenReturn(BOT_ANSWER);
        AskRequest request = new AskRequest();
        request.setMessage("Hello, what time is church?");
        request.setSessionId(UUID.randomUUID());

        AskResponse response = new AskResponse("Church starts at 10am.");
        Mockito.when(askService.processAsk(any(AskRequest.class), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok(response));

        // Act + Assert
        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Api-Key", "valid-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Church starts at 10am."));
    }

    @Test
    void ask_withMissingApiKey_returnsUnauthorized() throws Exception {
        AskRequest request = new AskRequest();
        request.setMessage("No key");
        request.setSessionId(UUID.randomUUID());

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ask_withEmptyMessage_returnsBadRequest() throws Exception {
        AskRequest request = new AskRequest();
        request.setMessage(" ");
        request.setSessionId(UUID.randomUUID());
        when(apiKeyService.getActiveChurchesByApiKey(VALID_API_KEY)).thenReturn(Optional.of(churchEntity));
        AskResponse response = new AskResponse("The message is empty.");
        Mockito.when(askService.processAsk(any(AskRequest.class), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.badRequest().body(response));

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Api-Key", "valid-api-key"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response").value("The message is empty."));
    }
}
