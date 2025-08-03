package com.erikmikac.ChapelChat.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.config.OpenAiProperties;
import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.ChatLogService;
import com.erikmikac.ChapelChat.service.ChurchApiKeyService;
import com.erikmikac.ChapelChat.service.ChurchProfileService;
import com.erikmikac.ChapelChat.service.InputSanitizationService;
import com.erikmikac.ChapelChat.service.OpenAiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/ask")
@Slf4j
public class AskController {

    private final OpenAiProperties openAiProperties;
    private final int MAX_CHARACTERS = 500;

    // 🛎️ Services
    private final ChatLogService chatLogService;
    private final ChurchProfileService profileService;
    private final OpenAiService aiService;
    private final ChurchApiKeyService apiKeyService;
    private final InputSanitizationService sanitizer;

    public AskController(ChurchProfileService profileService,
            OpenAiService aiService,
            ChatLogService chatLogService,
            ChurchApiKeyService apiKeyService,
            InputSanitizationService sanitizer, OpenAiProperties openAiProperties) {
        this.profileService = profileService;
        this.aiService = aiService;
        this.chatLogService = chatLogService;
        this.apiKeyService = apiKeyService;
        this.sanitizer = sanitizer;
        this.openAiProperties = openAiProperties;
    }

    @PostMapping
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest askRequest, HttpServletRequest request) {
        Optional<ResponseEntity<AskResponse>> maybeError = validateAskRequest(askRequest);
        if (maybeError.isPresent())
            return maybeError.get();

        final String ip = extractClientIp(request);
        final String apiKey = request.getHeader("X-Api-Key");

        final Optional<String> churchIdOpt = getChurchIdFromApiKey(apiKey);
        if (churchIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AskResponse("Invalid or revoked API key."));
        }

        final String churchId = churchIdOpt.get();
        if (!sanitizer.isSafe(askRequest.getMessage())) {
            return handleUnsafeMessage(askRequest, churchId, ip);
        }

        try {
            final String userAgent = request.getHeader("User-Agent");
            final String question = askRequest.getMessage();
            final String prompt = profileService.getSystemPromptFor(churchId);
            final String answer = aiService.generateAnswer(prompt, question);

            final ChatLog chatLog = buildChatLog(askRequest, churchId, ip, userAgent, question, answer);

            if (chatLogService.isMaxSessionCountReached(askRequest, ip)) {
                return handleRateLimitExceeded(chatLog, "question_limit_per_session_exceeded",
                        "You've reached the maximum number of questions for this session.");
            }

            if (chatLogService.isTooManyRequestsFromSameIP(ip)) {
                return handleRateLimitExceeded(chatLog, "rate_limit_exceeded",
                        "Question could not be processed. Please try again later.");
            }

            chatLogService.saveChatLogAndEnrich(chatLog);
            return ResponseEntity.ok(new AskResponse(answer));

        } catch (ChurchProfileNotFoundException e) {
            return ResponseEntity.badRequest().body(new AskResponse("That church's profile could not be found."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new AskResponse("An unexpected error occurred."));
        }
    }

    private Optional<ResponseEntity<AskResponse>> validateAskRequest(AskRequest askRequest) {
        if (askRequest.getMessage() == null || askRequest.getMessage().trim().isEmpty()) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(new AskResponse("The message is empty.")));
        }
        return Optional.empty();
    }

    private String extractClientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(s -> s.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }

    private Optional<String> getChurchIdFromApiKey(String apiKey) {
        return apiKeyService.getChurchIdForValidKey(apiKey);
    }

    private ResponseEntity<AskResponse> handleUnsafeMessage(AskRequest askRequest, String churchId, String ip) {
        ChatLog chatLog = ChatLog.builder()
                .churchId(churchId)
                .sourceIp(ip)
                .sessionId(askRequest.getSessionId())
                .userQuestion(askRequest.getMessage())
                .botResponse("Blocked for unsafe content.")
                .metadata(Map.of(
                        ChatLogMetadataKey.FLAGGED.key(), true,
                        ChatLogMetadataKey.FLAG_REASON.key(), "prompt_injection"))
                .timestamp(Instant.now())
                .build();

        chatLogService.saveChatLog(chatLog);
        log.warn("Prompt injection attempt detected: {}", askRequest.getMessage());

        return ResponseEntity.ok(new AskResponse("Sorry, that message can't be processed."));
    }

    private ResponseEntity<AskResponse> handleRateLimitExceeded(ChatLog chatLog, String reason, String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put(ChatLogMetadataKey.FLAGGED.key(), true);
        meta.put(ChatLogMetadataKey.FLAG_REASON.key(), reason);
        chatLog.setMetadata(meta);
        chatLogService.saveChatLog(chatLog);

        return ResponseEntity.badRequest().body(new AskResponse(message));
    }

    private ChatLog buildChatLog(
            AskRequest askRequest,
            String churchId,
            String ip,
            String userAgent,
            String userMessage,
            String botResponse) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ChatLogMetadataKey.OPENAI_MODEL.key(), this.openAiProperties.getModel());
        metadata.put(ChatLogMetadataKey.TEMPERATURE.key(), openAiProperties.getTemperature());
        metadata.put(ChatLogMetadataKey.USER_AGENT.key(), userAgent);

        return ChatLog.builder()
                .churchId(churchId)
                .sessionId(askRequest.getSessionId())
                .sourceIp(ip)
                .userQuestion(userMessage)
                .botResponse(botResponse)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
    }

}
