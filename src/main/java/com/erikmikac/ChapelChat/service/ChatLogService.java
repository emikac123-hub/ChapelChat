package com.erikmikac.ChapelChat.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;
import com.erikmikac.ChapelChat.service.admin.ChurchProfileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatLogService {
    private final JavaMailSender mailSender;
    private final ChurchProfileService profileService;
    private final ChatLogRepository chatLogRepository;
    private final OpenAiService aiService;
    final int MAX_QUESTIONS_PER_SESSION = 10;
    final int MAX_QUESTIONS_PER_IP = 50;

    @Transactional
    public void saveChatLog(final ChatLog chatLog) {
        chatLogRepository.save(chatLog);
    }

    @Async
    public void saveChatLogAndEnrichAsync(ChatLog chatLog, String requestId) {
        // Save initial log
        log.info("[{}] Saving chat log for session ID: {}", requestId, chatLog.getSessionId());
        saveChatLog(chatLog);
        // Run enrichment in background
        enrichMetadata(chatLog, requestId);
        this.sendFlagAlertIfNeeded(chatLog, requestId);
    }

    public void enrichMetadata(ChatLog chatLog, String requestId) {
        try {
            Map<String, Object> metadata = aiService.analyzeMetadata(chatLog.getUserQuestion());
            chatLog.setMetadata(metadata);
            saveChatLog(chatLog);
            log.debug("[{}] Metadata enrichment complete for ChatLog {}", requestId, chatLog.getId());
        } catch (Exception e) {
            log.warn("[{}] Metadata enrichment failed for ChatLog {}", requestId, chatLog.getId(), e);
        }
    }

    @Async
    public void sendFlagAlertIfNeeded(ChatLog chatLog, String requestId) {
        Map<String, Object> meta = chatLog.getMetadata();
        if (meta == null || !Boolean.TRUE.equals(meta.get(ChatLogMetadataKey.FLAGGED.key()))) {
            log.debug("[{}] No flag present — skipping alert email for chatLog {}", requestId, chatLog.getId());
            return;
        }

        String reason = String.valueOf(meta.get(ChatLogMetadataKey.FLAG_REASON.key()));
        String contactEmail = profileService.getContactEmailFor(chatLog.getChurchId());
        if (contactEmail == null || contactEmail.isBlank()) {
            log.warn("[{}] Flagged message detected, but no contact email found for churchId={}", requestId,
                    chatLog.getChurchId());
            return;
        }

        String subject = "⚠️ ChapelChat flagged message alert";
        String body = """
                A flagged message was detected for %s:

                ❓ User Message:
                %s

                📢 Bot Response:
                %s

                🚩 Flag Reason:
                %s

                🕓 Time:
                %s

                Session ID: %s
                IP: %s
                """.formatted(
                chatLog.getChurchId(),
                chatLog.getUserQuestion(),
                chatLog.getBotResponse(),
                reason,
                chatLog.getTimestamp(),
                chatLog.getSessionId(),
                chatLog.getSourceIp());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(contactEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("[{}] Flag alert email sent to {} for chatLog {}", requestId, contactEmail, chatLog.getId());
        } catch (Exception e) {
            log.error("[{}] Failed to send flag alert email to {} for chatLog {}", requestId, contactEmail,
                    chatLog.getId(), e);
        }
    }

    public boolean isMaxSessionCountReached(final AskRequest askRequest, String ip, String requestId) {
        if (askRequest.getSessionId() != null) {
            int questionsSoFar = chatLogRepository.countBySessionId(askRequest.getSessionId());
            final var isTooManyQuestions = questionsSoFar >= MAX_QUESTIONS_PER_SESSION;
            if (isTooManyQuestions) {
                log.warn("[{}] Session {} (IP: {}) has exceeded max questions ({}). Count: {}.",
                        requestId,
                        askRequest.getSessionId(),
                        ip,
                        MAX_QUESTIONS_PER_SESSION,
                        questionsSoFar);
                return true;
            }
        }
        return false;
    }

    public boolean isTooManyRequestsFromSameIP(String ip, String requestId) {
        int requestsFromIp = chatLogRepository.countBySourceIpAndTimestampAfter(ip,
                Instant.now().minus(1, ChronoUnit.HOURS));
        final var isTooMany = requestsFromIp > MAX_QUESTIONS_PER_IP;
        if (isTooMany) {
            log.warn("[{}] Rate limit exceeded for IP {} with {} requests in the last hour", requestId, ip,
                    requestsFromIp);
            return true;
        }
        return false;
    }

}
