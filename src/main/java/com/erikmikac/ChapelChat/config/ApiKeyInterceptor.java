package com.erikmikac.ChapelChat.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.erikmikac.ChapelChat.model.admin.ResolvedKey;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final ApiKeyService apiKeyService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws IOException {

        // 1) Get key from either X-API-Key or Authorization
        final String presented = extractApiKey(request);
        if (presented == null || presented.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API Key");
            return false;
        }

        // 2) Resolve using the NEW service
        // Expected service method (recommended): Optional<ResolvedKey> resolve(String token)
        final Optional<ResolvedKey> resolved = Optional.of(apiKeyService.resolve(presented));

        // If your ApiKeyService currently returns Optional<ApiKey> instead,
        // you can temporarily adapt like this:
        // final Optional<ApiKey> resolved = apiKeyService.getActiveChurchesByApiKey(presented)
        //     .map(entity -> ResolvedKey.of(entity.getId(), entity.getChurchId(), ...));

        if (resolved.isEmpty()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API Key");
            return false;
        }

        // 3) Expose context to downstream layers
        ResolvedKey rk = resolved.get();
        request.setAttribute("apiKeyId", rk.id());            // UUID of key
        request.setAttribute("churchId", rk.churchId());      // String church/tenant id
        request.setAttribute("resolvedKey", rk);              // the whole DTO if useful

        // (Optional) If ApiKeyService throttles lastUsedAt updates internally,
        // nothing else to do here. Otherwise you could notify the service:
        // apiKeyService.touchLastUsed(rk.id());

        return true;
    }

    private static String extractApiKey(HttpServletRequest request) {
        // Prefer explicit header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) return apiKey.trim();

        // Fall back to Authorization: ApiKey <token> or Bearer <token>
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || auth.isBlank()) return null;

        String v = auth.trim();
        if (v.regionMatches(true, 0, "ApiKey ", 0, 7)) {
            return v.substring(7).trim();
        }
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return null;
    }
}
