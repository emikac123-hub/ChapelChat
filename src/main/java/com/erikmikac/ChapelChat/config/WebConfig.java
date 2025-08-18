package com.erikmikac.ChapelChat.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.erikmikac.ChapelChat.tenant.ChurchIdArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ChurchIdArgumentResolver resolver;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebConfig(ApiKeyInterceptor apiKeyInterceptor, ChurchIdArgumentResolver resolver) {
        this.resolver = resolver;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**") // Apply to all routes
                .excludePathPatterns("/public/**"); // Allow anonymous for specific endpoints
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}