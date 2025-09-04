package com.erikmikac.ChapelChat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.erikmikac.ChapelChat.config.ApiKeyInterceptor;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.AskService;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;
import com.erikmikac.ChapelChat.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(controllers = AskController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@Import(TestMvcConfig.class)
class AskControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AskService askService;

    @MockBean
    ApiKeyService apiKeyService;

    @MockBean
    ApiKeyInterceptor apiKeyInterceptor;

    @BeforeEach
    void setTenant() throws Exception {
        when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        TenantContext.set(new TenantContext.Context(
                /* orgId */ "org-1",
                /* tenantId */ "default",
                /* orgType */ TenantContext.OrgType.CHURCH));
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private String json(Object... kv) throws Exception {
        if (kv.length % 2 != 0)
            throw new IllegalArgumentException("pairs required");
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            map.put((String) kv[i], kv[i + 1]);
        return objectMapper.writeValueAsString(map);
    }

    @Test
    void ask_returns200_andBody_whenServiceOk() throws Exception {
        when(askService.processAsk(any(AskRequest.class), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok(new AskResponse("10am on Sunday")));

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("message", "What time is service?", "sessionId", UUID.randomUUID().toString()))
                .header("User-Agent", "JUnit/MockMvc")
                .with(req -> {
                    req.setRemoteAddr("198.51.100.10");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("10am on Sunday")));

        verify(askService).processAsk(any(AskRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void ask_returns400_whenServiceSaysBadRequest() throws Exception {
        when(askService.processAsk(any(AskRequest.class), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.badRequest().body(new AskResponse("The message is empty.")));

        mockMvc.perform(post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("message", " ", "sessionId", UUID.randomUUID().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("empty")));
    }
}
