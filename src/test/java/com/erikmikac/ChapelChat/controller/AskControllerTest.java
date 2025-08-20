package com.erikmikac.ChapelChat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.AskService;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;
import com.erikmikac.ChapelChat.model.admin.ResolvedKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(controllers = AskController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class AskControllerTest {

    @Autowired MockMvc mockMvc;
    
    @Autowired ObjectMapper objectMapper;

    @MockBean
    AskService askService;

    @MockBean
    ApiKeyService apiKeyService;

    private String json(Object... kv) throws Exception {
        // quick helper to build JSON from pairs: "key","value","k2","v2",...
        if (kv.length % 2 != 0)
            throw new IllegalArgumentException("pairs required");
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            map.put((String) kv[i], kv[i + 1]);
        return objectMapper.writeValueAsString(map);
    }

    @Test
    public void ask_returns200_andBody_whenServiceOk() throws Exception {
        when(askService.processAsk(any(AskRequest.class), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok(new AskResponse("10am on Sunday")));

        when(apiKeyService.resolve(anyString())).thenReturn(new ResolvedKey("k1", "church-1"));

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("message", "What time is service?", "sessionId", UUID.randomUUID().toString()))
                .header("X-Api-Key", "k1")
                .header("User-Agent", "JUnit/MockMvc")
                // set client IP for completeness
                .with(req -> {
                    req.setRemoteAddr("198.51.100.10");
                    return req;
                }).with(csrf()))
                .andExpect(status().isOk())
                // don’t assume field name in AskResponse; just check the text
                .andExpect(content().string(containsString("10am on Sunday")));

        verify(askService).processAsk(any(AskRequest.class), any(HttpServletRequest.class));

    }

}
