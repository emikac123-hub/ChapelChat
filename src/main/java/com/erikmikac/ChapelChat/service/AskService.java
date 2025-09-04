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
import com.erikmikac.ChapelChat.enums.OrgType;
import com.erikmikac.ChapelChat.exceptions.OrganizationProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskContext;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.model.FlagResponse;
import com.erikmikac.ChapelChat.model.PromptWithChecksum;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;
import com.erikmikac.ChapelChat.service.admin.OrganizationProfileService;
import com.erikmikac.ChapelChat.tenant.TenantContext;
import com.erikmikac.ChapelChat.util.ChatLogMetadataBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AskService {

    private final OpenAiProperties openAiProperties;
    private final ChatLogService chatLogService;
    private final OrganizationProfileService profileService;
    private final OpenAiService aiService;
    private final InputSanitizationService sanitizer;
    private final ApiKeyService apiKeyService; // optional legacy fallback

    public AskService(OrganizationProfileService profileService,
            OpenAiService aiService,
            ChatLogService chatLogService,
            ApiKeyService apiKeyService,
            InputSanitizationService sanitizer,
            OpenAiProperties openAiProperties) {
        this.profileService = profileService;
        this.aiService = aiService;
        this.chatLogService = chatLogService;
        this.apiKeyService = apiKeyService;
        this.sanitizer = sanitizer;
        this.openAiProperties = openAiProperties;
    }

    public ResponseEntity<AskResponse> processAsk(AskRequest askRequest, HttpServletRequest request) {
        final String requestId = UUID.randomUUID().toString();
        final String ip = extractClientIp(request);
        final String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("-");

        // --- Resolve org/tenant from TenantContext (preferred) ---
        var ctx = TenantContext.get();
        String orgId = ctx != null ? ctx.getOrgId() : null;
        String tenantId = ctx != null ? ctx.getTenantId() : null;
        var orgType = ctx != null ? ctx.getOrgType() : OrgType.CHURCH; // default for now

        // --- Legacy fallback: allow API key-only calls if needed ---
        if (orgId == null) {
            String presentedKey = firstNonBlank(request.getHeader("X-Api-Key"), request.getHeader("x-api-key"));
            if (!isBlank(presentedKey)) {
                var resolved = apiKeyService.resolve(presentedKey); // return orgId/tenantId/type
                if (resolved != null) {
                    orgId = resolved.orgId();
                    tenantId = resolved.tenantId();
                    orgType = resolved.orgType();
                }
            }
        }

        if (isBlank(orgId)) {
            log.warn("[{}] Unauthorized/unknown org. IP={}", requestId, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AskResponse("Invalid credentials."));
        }

        final String question = Optional.ofNullable(askRequest.getMessage()).orElse("").trim();
        if (question.isEmpty()) {
            return handleUnsafeMessage(new AskContext(askRequest, orgId, ip, userAgent, null, null, requestId, tenantId, orgType.name()));
        }

        // Pull the correct prompt for this org/tenant/type
        PromptWithChecksum prompt;
        try {
            prompt = profileService.getSystemPromptAndChecksumFor(orgId);
        } catch (OrganizationProfileNotFoundException e) {
            log.error("[{}] Profile not found for orgId={}, tenantId={}", requestId, orgId, tenantId, e);
            return ResponseEntity.badRequest().body(new AskResponse("That organization's profile could not be found."));
        } catch (Exception e) {
            log.error("[{}] Failed loading profile for orgId={}", requestId, orgId, e);
            return ResponseEntity.internalServerError().body(new AskResponse("An unexpected error occurred."));
        }

        var askCtx = new AskContext(askRequest,
                orgId,
                ip,
                userAgent,
                prompt.systemPrompt(), prompt.profileChecksum(), requestId, tenantId, orgType.name());

        // Safety checks
        if (!sanitizer.isSafe(question)) {
            return handleUnsafeMessage(askCtx); // logs and returns safe response
        }

        try {
            // Rate limits
            if (chatLogService.isMaxSessionCountReached(askRequest, ip, requestId)) {
                var flag = new FlagResponse("question_limit_per_session_exceeded",
                        "You've reached the maximum number of questions for this session.");
                return handleRateLimitExceeded(buildChatLogSkeleton(askCtx), flag, askCtx);
            }
            if (chatLogService.isTooManyRequestsFromSameIP(ip, requestId)) {
                var flag = new FlagResponse("rate_limit_exceeded",
                        "Question could not be processed. Please try again later.");
                return handleRateLimitExceeded(buildChatLogSkeleton(askCtx), flag, askCtx);
            }

            // Generate
            String answer = aiService.generateAnswer(prompt.systemPrompt(), question);

            // Persist chat log (async enrich continues as you had it)
            ChatLog chatLog = buildChatLog(askCtx, answer);
            chatLogService.saveChatLogAndEnrichAsync(chatLog, requestId);

            log.info("[{}] Ask processed for orgId={}, tenantId={}", requestId, orgId, tenantId);
            return ResponseEntity.ok(new AskResponse(answer));

        } catch (Exception e) {
            log.error("[{}] Unexpected error for orgId={}, tenantId={}", requestId, orgId, tenantId, e);
            return ResponseEntity.internalServerError().body(new AskResponse("An unexpected error occurred."));
        }
    }

    // ---------- helpers ----------

    private String extractClientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(s -> s.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : (!isBlank(b) ? b : null);
    }

    private static TenantContext.OrgType safeType(String t) {
        if (t == null)
            return TenantContext.OrgType.CHURCH;
        try {
            return TenantContext.OrgType.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TenantContext.OrgType.CHURCH;
        }
    }

    /** Build metadata skeleton without answer for flag flows */
    private ChatLog buildChatLogSkeleton(AskContext ctx) {
        Map<String, Object> metadata = ChatLogMetadataBuilder.create()
                .withProfileChecksum(ctx.profileChecksum())
                .withModel(openAiProperties.getModel())
                .withTemperature(openAiProperties.getTemperature())
                .withUserAgent(ctx.userAgent())
                .withOrgType(ctx.orgId()) // add this helper in builder if desired
                .build();

        return ChatLog.builder()
                .orgId(ctx.orgId()) // TODO: rename field to orgId in entity; temporarily mapping old getter
                .tenantId(ctx.tenantId()) // add in entity if you haven’t yet
                .sessionId(ctx.askRequest().getSessionId())
                .sourceIp(ctx.ip())
                .userQuestion(ctx.askRequest().getMessage())
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
    }

    private ChatLog buildChatLog(AskContext ctx, String botAnswer) {
        var log = buildChatLogSkeleton(ctx);
        log.setBotResponse(botAnswer);
        return log;
    }

    private ResponseEntity<AskResponse> handleRateLimitExceeded(final ChatLog chatLog,
            final FlagResponse flagResponse,
            final AskContext context) {
        // NOTE: your FlagResponse had a method named flagResaon(); if that’s a typo,
        // fix to flagReason()
        Map<String, Object> meta = ChatLogMetadataBuilder.create().withFlagged(true, flagResponse.flagResaon())
                .withProfileChecksum(context.profileChecksum()).withModel(openAiProperties.getModel())
                .withTemperature(openAiProperties.getTemperature()).withUserAgent(context.userAgent()).build();
        chatLog.setMetadata(meta);
        chatLogService.saveChatLog(chatLog);
        log.warn("[{}] Rate/limit hit. Q='{}'", chatLog.getId(), context.askRequest().getMessage());
        return ResponseEntity.badRequest().body(new AskResponse(flagResponse.botResponse()));
    }

    public ResponseEntity<AskResponse> handleUnsafeMessage(AskContext ctx) {
        ChatLog chatLog = ChatLog.builder().orgId(ctx.orgId()).sourceIp(ctx.ip())
                .sessionId(ctx.askRequest().getSessionId()).userQuestion(ctx.askRequest().getMessage())
                .botResponse("Blocked for unsafe content.").metadata(Map.of(ChatLogMetadataKey.FLAGGED.key(), true,
                        ChatLogMetadataKey.FLAG_REASON.key(), "prompt_injection"))
                .timestamp(Instant.now()).build();
        chatLogService.saveChatLog(chatLog);
        log.warn("[{}] Prompt injection attempt. Q='{}'", chatLog.getId(), ctx.askRequest().getMessage());
        return ResponseEntity.ok(new AskResponse("Sorry, that message can't be processed."));
    }

    // @Async must be public to be proxied
    @Async
    public void logPromptInjectionAsync(ChatLog chatLog, String profileChecksum, String userAgent) {
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
}