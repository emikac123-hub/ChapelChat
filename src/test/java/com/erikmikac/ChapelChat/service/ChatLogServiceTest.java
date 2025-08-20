package com.erikmikac.ChapelChat.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;
import com.erikmikac.ChapelChat.service.admin.ChurchProfileService;

@ExtendWith(MockitoExtension.class)
class ChatLogServiceTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    ChurchProfileService profileService;
    @Mock
    ChatLogRepository chatLogRepository;
    @Mock
    OpenAiService aiService;

    @InjectMocks
    ChatLogService service;
    private final UUID sessionId = UUID.fromString("a044dc8d-2f88-46a3-98b2-c89a41bf5fbb");

    private ChatLog mkChatLog() {
        ChatLog log = new ChatLog();
        log.setId(UUID.fromString("1f73db28-05d6-47d5-9874-a3f6531acd2c"));
        log.setChurchId("church-1");
        log.setSessionId(sessionId);
        log.setUserQuestion("Why is the sky blue? sess-abc");
        log.setBotResponse("Rayleigh scattering!");
        log.setTimestamp(Instant.parse("2025-08-19T12:34:56Z"));
        log.setSourceIp("203.0.113.10");
        return log;
    }

    @BeforeEach
    void resetStubs() {
        Mockito.reset(mailSender, profileService, chatLogRepository, aiService);
    }

    // ---------- saveChatLog / saveChatLogAndEnrichAsync ----------

    @Test
    void saveChatLog_persists() {
        ChatLog log = mkChatLog();
        service.saveChatLog(log);
        verify(chatLogRepository).save(log);
    }

    @Test
    void saveChatLogAndEnrichAsync_callsSaveThenEnrichThenAlert() {
        ChatLog log = mkChatLog();
        Map<String, Object> meta = Map.of("topic", "science");
        when(aiService.analyzeMetadata(log.getUserQuestion())).thenReturn(meta);
    
        service.saveChatLogAndEnrichAsync(log, "req-1");

        // initial save
        verify(chatLogRepository, atLeastOnce()).save(log);
        // enrichment saves again with metadata set
        assertEquals(meta, log.getMetadata());
        verify(chatLogRepository, atLeast(2)).save(log);
        // email path attempted (flag not set, so no send)
        verifyNoInteractions(mailSender);
    }

    // ---------- enrichMetadata ----------

    @Test
    void enrichMetadata_setsMetadataAndSaves() {
        ChatLog log = mkChatLog();
        Map<String, Object> meta = Map.of("topic", "apologetics");
        when(aiService.analyzeMetadata("Why is the sky blue? sess-abc")).thenReturn(meta);

        service.enrichMetadata(log, "req-2");

        assertEquals(meta, log.getMetadata());
        verify(chatLogRepository).save(log);
    }

    @Test
    void enrichMetadata_swallowedException_doesNotSave() {
        ChatLog log = mkChatLog();
        when(aiService.analyzeMetadata(anyString()))
                .thenThrow(new RuntimeException("AI down"));

        service.enrichMetadata(log, "req-3");

        assertNull(log.getMetadata(), "metadata should remain unset on failure");
        verify(chatLogRepository, never()).save(log);
    }

    // ---------- sendFlagAlertIfNeeded ----------

    @Test
    void sendFlagAlertIfNeeded_skipsWhenNoMetadata() {
        ChatLog log = mkChatLog();
        log.setMetadata(null);

        service.sendFlagAlertIfNeeded(log, "req-4");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendFlagAlertIfNeeded_skipsWhenNotFlagged() {
        ChatLog log = mkChatLog();
        log.setMetadata(Map.of("topic", "general",
                ChatLogMetadataKey.FLAGGED.key(), false));

        service.sendFlagAlertIfNeeded(log, "req-5");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendFlagAlertIfNeeded_skipsWhenNoContacts() {
        ChatLog log = mkChatLog();
        Map<String, Object> meta = new HashMap<>();
        meta.put(ChatLogMetadataKey.FLAGGED.key(), true);
        meta.put(ChatLogMetadataKey.FLAG_REASON.key(), "TOS violation");
        log.setMetadata(meta);

        when(profileService.getContactEmailFor("church-1")).thenReturn(Collections.emptySet());

        service.sendFlagAlertIfNeeded(log, "req-6");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendFlagAlertIfNeeded_sendsEmailToAllContacts() {
        ChatLog log = mkChatLog();
        Map<String, Object> meta = new HashMap<>();
        meta.put(ChatLogMetadataKey.FLAGGED.key(), true);
        meta.put(ChatLogMetadataKey.FLAG_REASON.key(), "Prompt injection");
        log.setMetadata(meta);

        Set<String> contacts = Set.of("a@x.org", "b@y.org");
        when(profileService.getContactEmailFor("church-1")).thenReturn(contacts);

        service.sendFlagAlertIfNeeded(log, "req-7");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        List<SimpleMailMessage> sent = captor.getAllValues();
        assertEquals(2, sent.size());

        // Verify basics of the constructed email
        for (SimpleMailMessage msg : sent) {
            assertTrue(msg.getSubject().contains("flagged"), "subject should indicate flagged alert");
            assertTrue(msg.getText().contains("Rayleigh scattering!"), "body includes bot response");
            assertTrue(msg.getText().contains("Prompt injection"), "body includes flag reason");
            assertTrue(msg.getText().contains("sess-abc"), "body includes session id");
        }

        // Ensure each recipient was targeted
        Set<String> recipients = new HashSet<>();
        sent.forEach(m -> recipients.add(m.getTo()[0]));
        assertEquals(contacts, recipients);
    }

    @Test
    void sendFlagAlertIfNeeded_handlesSendFailures_perRecipient() {
        ChatLog log = mkChatLog();
        Map<String, Object> meta = Map.of(
                ChatLogMetadataKey.FLAGGED.key(), true,
                ChatLogMetadataKey.FLAG_REASON.key(), "Abuse");
        log.setMetadata(meta);

        Iterator<String> it = List.of("ok@x.org", "fail@y.org").iterator();
        when(profileService.getContactEmailFor("church-1"))
                .thenReturn(new LinkedHashSet<>(List.of("ok@x.org", "fail@y.org")));

        doNothing().when(mailSender)
                .send(Mockito.<SimpleMailMessage>argThat(m -> Objects.equals(m.getTo()[0], "ok@x.org")));

        doThrow(new RuntimeException("smtp fail")).when(mailSender)
                .send(Mockito.<SimpleMailMessage>argThat(m -> Objects.equals(m.getTo()[0], "fail@y.org")));

        assertDoesNotThrow(() -> service.sendFlagAlertIfNeeded(log, "req-8"));
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    // ---------- rate limiting ----------

    @Test
    void isMaxSessionCountReached_returnsFalseWhenNoSession() {
        AskRequest req = mock(AskRequest.class);
        when(req.getSessionId()).thenReturn(null);

        boolean result = service.isMaxSessionCountReached(req, "1.2.3.4", "req-9");
        assertFalse(result);
        verifyNoInteractions(chatLogRepository);
    }

    @Test
    void isMaxSessionCountReached_trueWhenCountAtOrAboveLimit() {
        AskRequest req = mock(AskRequest.class);
        when(req.getSessionId()).thenReturn(sessionId);
        when(chatLogRepository.countBySessionId(sessionId)).thenReturn(10); // limit is 10

        assertTrue(service.isMaxSessionCountReached(req, "1.2.3.4", "req-10"));
    }

    @Test
    void isMaxSessionCountReached_falseWhenBelowLimit() {
        AskRequest req = mock(AskRequest.class);
        when(req.getSessionId()).thenReturn(sessionId);
        when(chatLogRepository.countBySessionId(sessionId)).thenReturn(9);

        assertFalse(service.isMaxSessionCountReached(req, "1.2.3.4", "req-11"));
    }

    @Test
    void isTooManyRequestsFromSameIP_trueWhenOverLimit() {
        when(chatLogRepository.countBySourceIpAndTimestampAfter(eq("198.51.100.7"), any(Instant.class)))
                .thenReturn(51);

        assertTrue(service.isTooManyRequestsFromSameIP("198.51.100.7", "req-12"));
        // also ensure we passed a "last hour" cut-off Instant
        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(chatLogRepository).countBySourceIpAndTimestampAfter(eq("198.51.100.7"), captor.capture());
        Instant cutoff = captor.getValue();
        assertTrue(cutoff.isAfter(Instant.now().minus(2, ChronoUnit.HOURS)),
                "cutoff should be roughly within last hour");
    }

    @Test
    void isTooManyRequestsFromSameIP_falseAtOrUnderLimit() {
        when(chatLogRepository.countBySourceIpAndTimestampAfter(eq("198.51.100.7"), any(Instant.class)))
                .thenReturn(50); // limit is strictly '>' 50

        assertFalse(service.isTooManyRequestsFromSameIP("198.51.100.7", "req-13"));
    }
}