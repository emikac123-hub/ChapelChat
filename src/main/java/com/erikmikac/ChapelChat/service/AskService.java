package com.erikmikac.ChapelChat.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.config.OpenAiProperties;
import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;
import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskContext;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.model.FlagResponse;
import com.erikmikac.ChapelChat.model.PromptWithChecksum;
import com.erikmikac.ChapelChat.model.admin.ResolvedKey;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;
import com.erikmikac.ChapelChat.service.admin.ChurchProfileService;
import com.erikmikac.ChapelChat.util.ChatLogMetadataBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AskService {

    private final OpenAiProperties openAiProperties;

    // 🛎️ Services
    private final ChatLogService chatLogService;
    private final ChurchProfileService profileService;
    private final OpenAiService aiService;
    private ApiKeyService apiKeyService;
    private final InputSanitizationService sanitizer;

    public AskService(ChurchProfileService profileService,
            OpenAiService aiService,
            ChatLogService chatLogService,
            ApiKeyService apiKeyService,
            InputSanitizationService sanitizer, OpenAiProperties openAiProperties) {
        this.profileService = profileService;
        this.aiService = aiService;
        this.chatLogService = chatLogService;
        this.apiKeyService = apiKeyService;
        this.sanitizer = sanitizer;
        this.openAiProperties = openAiProperties;
    }

    public ResponseEntity<AskResponse> processAsk(AskRequest askRequest, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Ask request received for session: {}", requestId, askRequest.getSessionId());

        String ip = extractClientIp(request);
        String apiKey = request.getHeader("X-Api-Key");
        Optional<String> churchIdOpt = getChurchIdFromApiKey(apiKey);

        if (churchIdOpt.isEmpty()) {
            log.warn("[{}] Invalid API key from IP: {}", requestId, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AskResponse("Invalid or revoked API key."));
        }
        String churchId = churchIdOpt.get();
        String userAgent = request.getHeader("User-Agent");
        String question = askRequest.getMessage();
        Optional<ResponseEntity<AskResponse>> maybePromptInjection = validateAskRequest(askRequest);
        PromptWithChecksum prompt = null;

        try {
            prompt = profileService.getSystemPromptAndChecksumFor(churchId);
        } catch (ChurchProfileNotFoundException e) {
            log.error("[{}] Could not find church profile for churchId: {}", requestId, churchId, e);
            return ResponseEntity.badRequest().body(new AskResponse("That church's profile could not be found."));
        }

        var askContext = new AskContext(askRequest, churchId, ip, userAgent, prompt.systemPrompt(),
                prompt.profileChecksum(), requestId);

        if (!sanitizer.isSafe(askRequest.getMessage())) {
            return handleUnsafeMessage(askContext);
        }

        try {

            String answer = aiService.generateAnswer(prompt.systemPrompt(), question);

            ChatLog chatLog = buildChatLog(askContext);

            if (maybePromptInjection.isPresent()) {
                handlePromptInjection(chatLog, churchId, userAgent);
                return maybePromptInjection.get();
            }
            if (chatLogService.isMaxSessionCountReached(askRequest, ip, askContext.requestId())) {
                var flagResponse = new FlagResponse("question_limit_per_session_exceeded",
                        "You've reached the maximum number of questions for this session.");
                return handleRateLimitExceeded(chatLog, flagResponse,
                        askContext);
            }

            if (chatLogService.isTooManyRequestsFromSameIP(ip, askContext.requestId())) {
                final var flagResponse = new FlagResponse("rate_limit_exceeded",
                        "Question could not be processed. Please try again later.");
                return handleRateLimitExceeded(chatLog, flagResponse, askContext);
            }

            chatLogService.saveChatLogAndEnrichAsync(chatLog, requestId);
            log.info("[{}] Successfully processed ask request for church: {}", requestId, churchId);
            return ResponseEntity.ok(new AskResponse(answer));

        } catch (Exception e) {
            log.error("[{}] An unexpected error occurred for churchId: {}", requestId, churchId, e);
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

    private Optional<String> getChurchIdFromApiKey(String presentedKey) {
        ResolvedKey apiKey = apiKeyService.resolve(presentedKey);
        return Optional.ofNullable(apiKey)
                .map(ResolvedKey::churchId);
    }

    private ResponseEntity<AskResponse> handleUnsafeMessage(AskContext context) {
        ChatLog chatLog = ChatLog.builder()
                .churchId(context.churchId())
                .sourceIp(context.ip())
                .sessionId(context.askRequest().getSessionId())
                .userQuestion(context.askRequest().getMessage())
                .botResponse("Blocked for unsafe content.")
                .metadata(Map.of(
                        ChatLogMetadataKey.FLAGGED.key(), true,
                        ChatLogMetadataKey.FLAG_REASON.key(), "prompt_injection"))
                .timestamp(Instant.now())
                .build();

        chatLogService.saveChatLog(chatLog);
        log.warn("[{}] Prompt injection attempt detected  Review Chat Logs for details: {}", chatLog.getId(),
                context.askRequest().getMessage());

        return ResponseEntity.ok(new AskResponse("Sorry, that message can't be processed."));
    }

    private ResponseEntity<AskResponse> handleRateLimitExceeded(final ChatLog chatLog,
            final FlagResponse flagResponse,
            final AskContext context) {

        Map<String, Object> meta = ChatLogMetadataBuilder.create()
                .withFlagged(true, flagResponse.flagResaon())
                .withProfileChecksum(context.profileChecksum())
                .withModel(openAiProperties.getModel())
                .withTemperature(openAiProperties.getTemperature())
                .withUserAgent(context.userAgent())
                .build();
        chatLog.setMetadata(meta);
        chatLogService.saveChatLog(chatLog);
        log.warn("[{}] Limit rate exceeded. Review Chat Logs for details: {}", chatLog.getId(),
                context.askRequest().getMessage());
        return ResponseEntity.badRequest().body(new AskResponse(flagResponse.botResponse()));
    }

    @Async
    private void handlePromptInjection(ChatLog chatLog,
            String profileChecksum, String userAgent) {

        Map<String, Object> meta = ChatLogMetadataBuilder.create()
                .withFlagged(true, "prompt_injection")
                .withProfileChecksum(profileChecksum)
                .withModel(openAiProperties.getModel())
                .withTemperature(openAiProperties.getTemperature())
                .withUserAgent(userAgent)

                .build();
        chatLog.setMetadata(meta);
        chatLogService.saveChatLog(chatLog);

    }

    private ChatLog buildChatLog(
            AskContext context) {
        Map<String, Object> metadata = ChatLogMetadataBuilder.create()
                .withProfileChecksum(context.profileChecksum())
                .withModel(openAiProperties.getModel())
                .withTemperature(openAiProperties.getTemperature())
                .withUserAgent(context.userAgent())
                .build();

        return ChatLog.builder()
                .churchId(context.churchId())
                .sessionId(context.askRequest().getSessionId())
                .sourceIp(context.ip())
                .userQuestion(context.askRequest().getMessage())
                .botResponse(context.prompt())
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
    }
}
