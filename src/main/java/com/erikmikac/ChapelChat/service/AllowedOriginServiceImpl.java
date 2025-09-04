package com.erikmikac.ChapelChat.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service 
class AllowedOriginServiceImpl implements AllowedOriginService {
    @Override
    public List<String> getAllowedOrigins(String tenantId, String orgId) {
        // TODO: fetch from DB/config
        return List.of("https://example.com");
    }
}