package com.erikmikac.ChapelChat.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.erikmikac.ChapelChat.tenant.TenantArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor; // <— keep your existing interceptor
    private final TenantArgumentResolver tenantArgumentResolver; // <— neutral, replaces ChurchIdArgumentResolver

    public WebConfig(ApiKeyInterceptor apiKeyInterceptor,
            TenantArgumentResolver tenantArgumentResolver) {
        this.apiKeyInterceptor = apiKeyInterceptor;
        this.tenantArgumentResolver = tenantArgumentResolver;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/public/**",
                        "/error",
                        "/actuator/**",
                        "/health",
                        "/v3/api-docs/**",
                        "/swagger-ui/**");
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        
        resolvers.add(tenantArgumentResolver);
     
    }

}