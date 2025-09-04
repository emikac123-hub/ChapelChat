package com.erikmikac.ChapelChat.controller;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.erikmikac.ChapelChat.tenant.TenantArgumentResolver;

/** Minimal config to register your neutral resolver in a MVC slice test. */
@TestConfiguration
public class TestMvcConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TenantArgumentResolver());
    }
}
