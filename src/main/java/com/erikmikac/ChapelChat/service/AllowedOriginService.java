package com.erikmikac.ChapelChat.service;

import java.util.List;

import org.springframework.stereotype.Service;
@Service
public interface AllowedOriginService {
    List<String> getAllowedOrigins(String tenantId, String orgId);
}