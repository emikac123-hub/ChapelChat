package com.erikmikac.ChapelChat.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.erikmikac.ChapelChat.entity.ChurchApiKeyEntity;
import com.erikmikac.ChapelChat.repository.ChurchApiKeyRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final ChurchApiKeyRepository apiKeyRepository;

    public ApiKeyInterceptor(ChurchApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("Header: " + headerName + " = " + request.getHeader(headerName));
        }

        String apiKey = request.getHeader("X-Api-Key"); // this is case-insensitive in most servers
        System.out.println("Extracted API Key: " + apiKey);

        if (apiKey == null || apiKey.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API Key");
            return false;
        }

        Optional<ChurchApiKeyEntity> match = apiKeyRepository.findByApiKey(apiKey);
        if (match.isEmpty()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API Key");
            return false;
        }

        request.setAttribute("church", match.get().getChurch());
        return true;
    }

}
