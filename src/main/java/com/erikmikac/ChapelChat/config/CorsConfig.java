package com.erikmikac.ChapelChat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/ask")
                        .allowedOrigins("http://localhost:3000", "https://super-haupia-aa6453.netlify.app") 
                        
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("Content-Type", "X-Api-Key")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
