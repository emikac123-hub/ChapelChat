package com.erikmikac.ChapelChat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.erikmikac.ChapelChat.tenant.TenantContext;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class CorsConfig {

    @Bean(name = "chapelCorsConfigurationSource") // avoid default name collision
    @SuppressWarnings("Convert2Lambda")
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                // === your existing logic, just not as a lambda ===
                boolean isLocal = false; // put your profile check here if needed

                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                cfg.setAllowedHeaders(java.util.List.of(
                    "Content-Type","Accept","Authorization","X-API-Key","X-Api-Key"));
                cfg.setExposedHeaders(java.util.List.of("Retry-After"));
                cfg.setMaxAge(1800L);

                if (isLocal) {
                    cfg.setAllowCredentials(false);
                    cfg.setAllowedOriginPatterns(java.util.List.of("http://localhost:*","http://127.0.0.1:*"));
                    return cfg;
                }

                var ctx = TenantContext.get();
                if (ctx == null || ctx.getOrgId() == null) {
                    return null; // deny (no CORS headers)
                }

                boolean usesCookies = false;
                java.util.List<String> origins =
                    /* allowedOriginService.getAllowedOrigins(ctx.getTenantId(), ctx.getOrgId()) */ java.util.List.of();

                cfg.setAllowCredentials(usesCookies);
                if (usesCookies) {
                    cfg.setAllowedOrigins(origins);          // exact origins only
                } else {
                    cfg.setAllowedOriginPatterns(origins);   // allow patterns when no cookies
                }
                return cfg;
            }
        };
    }
}