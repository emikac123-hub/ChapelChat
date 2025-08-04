package com.erikmikac.ChapelChat.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;

public class ChatLogServiceTest {

    private JavaMailSender mailSender;
    private ChurchProfileService profileService;
    private ChatLogRepository chatLogRepository;
    private OpenAiService aiService;

    private ChatLogService chatLogService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        profileService = mock(ChurchProfileService.class);
        chatLogRepository = mock(ChatLogRepository.class);
        aiService = mock(OpenAiService.class);

        chatLogService = new ChatLogService(mailSender, profileService, chatLogRepository, aiService);
    }

    @Test
    void sendFlagAlertIfNeeded_sendsEmailWhenFlaggedAndEmailExists() {
        // Arrange
        ChatLog chatLog = ChatLog.builder()
                .churchId("hope-baptist")
                .userQuestion("What the heck is this?")
                .botResponse("Let's keep things respectful.")
                .sessionId(UUID.randomUUID())
                .sourceIp("127.0.0.1")
                .timestamp(Instant.now())
                .metadata(Map.of(
                        ChatLogMetadataKey.FLAGGED.key(), true,
                        ChatLogMetadataKey.FLAG_REASON.key(), "profanity"))
                .build();

        when(profileService.getContactEmailFor("hope-baptist")).thenReturn("pastor@example.com");

        // Act
        chatLogService.sendFlagAlertIfNeeded(chatLog, "123");

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("pastor@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("flagged message"));
        assertTrue(sentMessage.getText().contains("profanity"));
    }

    @Test
    void sendFlagAlertIfNeeded_skipsWhenNotFlagged() {
        ChatLog chatLog = ChatLog.builder()
                .churchId("hope-baptist")
                .metadata(Map.of(ChatLogMetadataKey.FLAGGED.key(), false))
                .build();

        chatLogService.sendFlagAlertIfNeeded(chatLog, "123");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendFlagAlertIfNeeded_skipsWhenNoEmailFound() throws InterruptedException, ExecutionException {
        ChatLog chatLog = ChatLog.builder()
                .churchId("hope-baptist")
                .metadata(Map.of(
                        ChatLogMetadataKey.FLAGGED.key(), true,
                        ChatLogMetadataKey.FLAG_REASON.key(), "distress"))
                .build();

        when(profileService.getContactEmailFor("hope-baptist")).thenReturn(null);

        chatLogService.sendFlagAlertIfNeeded(chatLog, "123");
        chatLogService.sendFlagAlertIfNeeded(chatLog, "123");
    

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}