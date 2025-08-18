package com.erikmikac.ChapelChat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                boolean isLocal = activeProfile != null &&
                        (activeProfile.contains("local") || activeProfile.contains("dev"));

                if (isLocal) {
                    registry.addMapping("/**")
                            .allowedOriginPatterns("http://localhost:*")
                            .allowedMethods("*")
                            .allowedHeaders("*")
                            .allowCredentials(false)
                            .maxAge(3600);
                    return;
                }

                // Public widget endpoint
                registry.addMapping("/ask")
                        .allowedOriginPatterns("http://localhost:*", "https://*.netlify.app")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("Content-Type", "Accept", "X-API-KEY")
                        .exposedHeaders() // add if you need to expose any
                        .allowCredentials(false)
                        .maxAge(3600);

                // Future dashboard API (JWT / cookies)
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("http://localhost:*", "https://*.yourdashboard.tld")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Accept", "Authorization")
                        .exposedHeaders()
                        .allowCredentials(true) // if you go cookie-based; set to false if using pure Bearer tokens
                        .maxAge(3600);
            }
        };
    }
}
