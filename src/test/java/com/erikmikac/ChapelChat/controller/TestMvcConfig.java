package com.erikmikac.ChapelChat.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestMvcConfig {

    @Bean
    public WebMvcConfigurer testCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:*")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}
