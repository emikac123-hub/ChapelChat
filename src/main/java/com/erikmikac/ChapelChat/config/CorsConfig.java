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
                if ("local".equals(activeProfile)) {
                    registry.addMapping("/**")
                            .allowedOrigins("*")
                            .allowedMethods("*")
                            .allowedHeaders("*");
                } else {
                    registry.addMapping("/ask")
                            .allowedOrigins("http://localhost:3000", "http://localhost:5176","http://localhost:5177", "https://super-haupia-aa6453.netlify.app")
                            .allowedMethods("POST", "OPTIONS")
                            .allowedHeaders("Content-Type", "X-Api-Key")
                            .allowCredentials(false)
                            .maxAge(3600);
                }
            }
        };
    }
}
