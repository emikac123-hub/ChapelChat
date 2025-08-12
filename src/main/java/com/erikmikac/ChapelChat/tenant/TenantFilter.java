package com.erikmikac.ChapelChat.tenant;

import java.io.IOException;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.erikmikac.ChapelChat.repository.ChurchRepository;
import com.erikmikac.ChapelChat.service.ChurchApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final ChurchRepository churchRepository;
    private final ChurchApiKeyService churchApiKeyService;

    public TenantFilter(final ChurchRepository churchRepository,final ChurchApiKeyService churchApiKeyService) {
        this.churchRepository = churchRepository;
        this.churchApiKeyService = churchApiKeyService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            // Example: extract from an authenticated principal / claim / interceptor
            String churchId = extractFromSecurityContextOrSubdomain(req);
            if (churchId == null) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Church not resolved");
                return;
            }
            TenantContext.setChurchId(churchId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear(); // important for thread reuse
        }
    }

    @Nullable
    private String extractFromSecurityContextOrSubdomain(HttpServletRequest req) {
        // 1) From Spring Security (JWT claim set by your auth filter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("church_id");
            if (StringUtils.hasText(claim))
                return claim;
        }
        // (Alt) if you have a custom Principal that carries tenant info:
        if (auth != null && auth.getPrincipal() instanceof HasChurchId p) {
            String id = p.getChurchId();
            if (StringUtils.hasText(id))
                return id;
        }

        // 2) From API key header (if you support API-key auth to the admin/API)
        // NOTE: if your API key logic currently lives in a HandlerInterceptor,
        // this filter runs BEFORE it. Either (a) move API key auth into a security
        // filter,
        // or (b) resolve the key here as shown.
        String apiKey = req.getHeader("X-Api-Key");
        if (!StringUtils.hasText(apiKey)) {
            apiKey = req.getHeader("x-api-key"); // be liberal in what you accept
        }
        if (StringUtils.hasText(apiKey)) {
            String id = churchApiKeyService.getChurchIdForValidKey(apiKey).orElseThrow(); // implement lookup + timing-safe compare
            if (StringUtils.hasText(id))
                return id;
        }

        // 3) Fallback: derive from subdomain (useful for public widget traffic)
        String host = firstNonNull(req.getHeader("X-Forwarded-Host"), req.getServerName());
        String sub = extractSubdomain(host); // see helper below
        if (StringUtils.hasText(sub) && churchRepository.existsById(sub)) {
            return sub;
        }

        // Nothing worked
        return null;
    }

    private static String firstNonNull(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    @Nullable
    private static String extractSubdomain(String host) {
        if (!StringUtils.hasText(host))
            return null;
        String h = host.toLowerCase().split(":")[0]; // strip port
        // localhost dev: e.g., hope-baptist.localhost
        if (h.endsWith(".localhost")) {
            return h.substring(0, h.indexOf(".localhost"));
        }
        String[] parts = h.split("\\.");
        if (parts.length < 3)
            return null; // no subdomain (e.g., chapel.chat)
        String first = parts[0];
        if (first.equals("www") || first.equals("app"))
            return null;
        return first; // e.g., "hope-baptist"
    }
}
