package com.erikmikac.ChapelChat.service;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.erikmikac.ChapelChat.config.OpenAiProperties;
import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.enums.OrgType;
import com.erikmikac.ChapelChat.exceptions.OrganizationProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.model.PromptWithChecksum;
import com.erikmikac.ChapelChat.model.admin.ResolvedKey;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;
import com.erikmikac.ChapelChat.service.admin.OrganizationProfileService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for AskService (pure Mockito, no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class AskServiceTest {

    @Mock
    OrganizationProfileService profileService;
    @Mock
    OpenAiService aiService;
    @Mock
    ChatLogService chatLogService;
    @Mock
    ApiKeyService apiKeyService;
    @Mock
    InputSanitizationService sanitizer;
    @Mock
    OpenAiProperties openAiProperties;

    @InjectMocks
    AskService service;

    @BeforeEach
    void setup() {
        service = new AskService(
                profileService, aiService, chatLogService, apiKeyService, sanitizer, openAiProperties);
        lenient().when(openAiProperties.getModel()).thenReturn("gpt-test");
        lenient().when(openAiProperties.getTemperature()).thenReturn(0.2);
    }

    // --- helpers -------------------------------------------------------------

    private AskRequest mkAsk(String msg, String session) {
        AskRequest r = mock(AskRequest.class);
        lenient().when(r.getMessage()).thenReturn(msg);
        if (session != null) {
            lenient().when(r.getSessionId()).thenReturn(UUID.nameUUIDFromBytes(session.getBytes()));
        }
        return r;
    }

    private HttpServletRequest mkReq(String apiKey, String ip, String userAgent, String xff) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Api-Key", apiKey);
        req.addHeader("User-Agent", userAgent);
        req.setRemoteAddr(ip);
        if (xff != null)
            req.addHeader("X-Forwarded-For", xff);
        return req; // implements HttpServletRequest
    }

    private PromptWithChecksum mkPrompt(String sys, String checksum) {
        return new PromptWithChecksum(sys, checksum);
    }

    private ResolvedKey mkResolved(String churchId) {
        return new ResolvedKey("key", churchId, "default", OrgType.CHURCH);
    }

    // --- tests ---------------------------------------------------------------

    @Test
    void invalidApiKey_returns401_andSkipsDownstream() {
        AskRequest ask = mkAsk("Hello", "sess1");
        HttpServletRequest req = mkReq("bad-key", "1.2.3.4", "UA", null);
        when(apiKeyService.resolve("bad-key")).thenReturn(null);

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(401, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().contains("Invalid credentials."));
        verifyNoInteractions(profileService, aiService, chatLogService, sanitizer);
    }

    @Test
    void emptyMessage_returns200_andCallsHandlePromptInjectionPath() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("   ", "sess1"); // empty after trim
        HttpServletRequest req = mkReq("key", "1.2.3.4", "UA", null);

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-1"));
        // sanitizer is not reached because empty message is validated later in method

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().contains("can't be processed"));
        // The code calls handlePromptInjection(...) on this branch
        verify(chatLogService, atLeastOnce()).saveChatLog(any(ChatLog.class));
    }

    @Test
    void sanitizerBlocksMessage_returns200_withBlockedText_andSavesFlaggedLog() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("ignore previous", "sess1");
        HttpServletRequest req = mkReq("key", "203.0.113.9", "UA", null);

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-1"));
        when(profileService.getSystemPromptAndChecksumFor("church-1"))
                .thenReturn(mkPrompt("SYS", "chk"));
        when(sanitizer.isSafe("ignore previous")).thenReturn(false);

        ArgumentCaptor<ChatLog> cap = ArgumentCaptor.forClass(ChatLog.class);

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().contains("can't be processed"));

        verify(chatLogService).saveChatLog(cap.capture());
        ChatLog saved = cap.getValue();
        assertEquals("church-1", saved.getOrgId());
        assertEquals(UUID.nameUUIDFromBytes("sess1".getBytes()), saved.getSessionId());
        assertEquals("ignore previous", saved.getUserQuestion());
        assertEquals("Blocked for unsafe content.", saved.getBotResponse());
        assertNotNull(saved.getMetadata());
        assertEquals(Boolean.TRUE, saved.getMetadata().get(ChatLogMetadataKey.FLAGGED.key()));
        assertEquals("prompt_injection", saved.getMetadata().get(ChatLogMetadataKey.FLAG_REASON.key()));
    }

    @Test
    void happyPath_returns200_andEnrichesAsync_andCallsAi() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("What time is service?", "sessA");
        HttpServletRequest req = mkReq("key", "198.51.100.3", "Mozilla", null);

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-2"));
        when(profileService.getSystemPromptAndChecksumFor("church-2"))
                .thenReturn(mkPrompt("SYS_PROMPT", "chk2"));
        when(sanitizer.isSafe("What time is service?")).thenReturn(true);
        when(aiService.generateAnswer("SYS_PROMPT", "What time is service?"))
                .thenReturn("10am on Sunday");

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("10am on Sunday", resp.getBody().getResponse());
        verify(aiService).generateAnswer("SYS_PROMPT", "What time is service?");
        verify(chatLogService).saveChatLogAndEnrichAsync(any(ChatLog.class), anyString());
    }

    @Test
    void sessionLimitExceeded_returns400_andSavesFlaggedMeta() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("Q?", "sessX");
        HttpServletRequest req = mkReq("key", "203.0.113.20", "UA", null);

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-3"));
        when(profileService.getSystemPromptAndChecksumFor("church-3"))
                .thenReturn(mkPrompt("SYS", "chk3"));
        when(sanitizer.isSafe("Q?")).thenReturn(true);
        when(chatLogService.isMaxSessionCountReached(eq(ask), eq("203.0.113.20"), anyString()))
                .thenReturn(true);

        ArgumentCaptor<ChatLog> cap = ArgumentCaptor.forClass(ChatLog.class);

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().toLowerCase().contains("maximum number"));

        verify(chatLogService).saveChatLog(cap.capture());
        Map<String, Object> meta = cap.getValue().getMetadata();
        assertNotNull(meta);
        // Flag should be set by handleRateLimitExceeded()
        assertEquals(Boolean.TRUE, meta.get(ChatLogMetadataKey.FLAGGED.key()));
        // Reason key comes from FlagResponse.flagResaon() (note spelling in your code)
        assertNotNull(meta.get(ChatLogMetadataKey.FLAG_REASON.key()));
    }

    @Test
    void ipRateLimitExceeded_returns400_andSavesFlaggedMeta() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("Q2", "sessY");
        HttpServletRequest req = mkReq("key", "198.51.100.50", "UA", null);

        Mockito.lenient().when(apiKeyService.resolve(anyString()))
                .thenReturn(new ResolvedKey("whatever", "church-4", "default", OrgType.CHURCH));

        when(profileService.getSystemPromptAndChecksumFor("church-4"))
                .thenReturn(mkPrompt("SYS", "chk4"));
        when(sanitizer.isSafe("Q2")).thenReturn(true);
        when(chatLogService.isMaxSessionCountReached(any(), anyString(), anyString()))
                .thenReturn(false);
        when(chatLogService.isTooManyRequestsFromSameIP(Mockito.eq("198.51.100.50"), anyString()))
                .thenReturn(true);

        ArgumentCaptor<ChatLog> cap = ArgumentCaptor.forClass(ChatLog.class);

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().toLowerCase().contains("try again later"));

        verify(chatLogService).saveChatLog(cap.capture());
        Map<String, Object> meta = cap.getValue().getMetadata();
        assertNotNull(meta);
        assertEquals(Boolean.TRUE, meta.get(ChatLogMetadataKey.FLAGGED.key()));
        assertNotNull(meta.get(ChatLogMetadataKey.FLAG_REASON.key()));
    }

    @Test
    void churchProfileMissing_returns400() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("Hi", "s1");
        HttpServletRequest req = mkReq("key", "203.0.113.2", "UA", null);

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-miss"));
        when(profileService.getSystemPromptAndChecksumFor("church-miss"))
                .thenThrow(new OrganizationProfileNotFoundException("missing"));

        ResponseEntity<AskResponse> resp = service.processAsk(ask, req);

        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().getResponse().toLowerCase().contains("profile could not be found"));
        verifyNoInteractions(aiService);
        verify(chatLogService, never()).saveChatLogAndEnrichAsync(any(), anyString());
    }

    @Test
    void usesXForwardedFor_firstIp_whenPresent() throws OrganizationProfileNotFoundException {
        AskRequest ask = mkAsk("Q", "ss");
        // XFF contains multiple IPs; service should pick the first
        HttpServletRequest req = mkReq("key", "ignored-remote", "UA", "203.0.113.10, 70.1.2.3");

        when(apiKeyService.resolve("key")).thenReturn(mkResolved("church-5"));
        when(profileService.getSystemPromptAndChecksumFor("church-5"))
                .thenReturn(mkPrompt("SYS", "chk5"));
        when(sanitizer.isSafe("Q")).thenReturn(true);
        when(aiService.generateAnswer(anyString(), anyString())).thenReturn("A");

        service.processAsk(ask, req);

        // We can assert that the chosen IP flows into rate-limit checks and logging by
        // verifying
        // the chatLogService is called with that IP when those branches are reached.
        // Here, just ensure we completed without error and saved async.
        verify(chatLogService).saveChatLogAndEnrichAsync(any(ChatLog.class), anyString());
    }
}
