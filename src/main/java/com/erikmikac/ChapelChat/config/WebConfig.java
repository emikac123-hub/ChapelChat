package com.erikmikac.ChapelChat.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
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
        // NOTE: Your TenantFilter (OncePerRequestFilter) should already run before
        // this,
        // so the interceptor can assume TenantContext is populated.
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        // Prefer the neutral resolver
        resolvers.add(tenantArgumentResolver);
        // If you need temporary back-compat for @CurrentChurchId, you can still
        // register the old resolver here too.
    }

    // (Optional) Static, permissive CORS for dev. For per-tenant origins, prefer a
    // CorsFilter using your AllowedOriginService.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        // .allowedOrigins("https://app.chapelchat.com", "https://*.yourcustomer.com")
        // // tighten in prod
    }
}