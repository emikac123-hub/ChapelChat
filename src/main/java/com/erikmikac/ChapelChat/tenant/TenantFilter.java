package com.erikmikac.ChapelChat.tenant;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.erikmikac.ChapelChat.service.admin.ApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilter extends OncePerRequestFilter {

  //  private final ApiKeyService apiKeys; // can be @Lazy if needed

    public TenantFilter(ApiKeyService apiKeys) {
   //     this.apiKeys = apiKeys;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            String churchId = extract(req);
            if (churchId == null || churchId.isBlank()) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Church not resolved");
                return;
            }
            TenantContext.setChurchId(churchId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    private String extract(HttpServletRequest req) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        // A) Logged-in admin (form login)
        if (auth != null && auth.getPrincipal() instanceof HasChurchId p) {
            return p.getChurchId();
        }

        // B) Widget/API with API key
        String apiKey = firstNonBlank(req.getHeader("X-Api-Key"), req.getHeader("x-api-key"));
        if (apiKey != null) {
            // var resolved = apiKeys.resolve(apiKey); // returns {id, churchId} or null
            // if (resolved != null)
            //     return resolved.churchId();
            return "";
        }

        // C) Optional: subdomain fallback (no DB call here)
        String host = java.util.Optional.ofNullable(req.getHeader("X-Forwarded-Host")).orElse(req.getServerName());
        return extractSubdomain(host);
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null && !b.isBlank() ? b : null);
    }

    private static String extractSubdomain(String host) {
        if (host == null)
            return null;
        var h = host.toLowerCase().split(":")[0];
        if (h.endsWith(".localhost"))
            return h.substring(0, h.indexOf(".localhost"));
        var parts = h.split("\\.");
        return parts.length >= 3 && !parts[0].equals("www") && !parts[0].equals("app") ? parts[0] : null;
    }
}
