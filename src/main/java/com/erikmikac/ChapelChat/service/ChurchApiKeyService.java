package com.erikmikac.ChapelChat.service;


import java.util.Optional;

import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.repository.ChurchApiKeyRepository;
import com.erikmikac.ChapelChat.security.ChurchApiKeyEntity;

@Service
public class ChurchApiKeyService {

    private final ChurchApiKeyRepository repository;

    public ChurchApiKeyService(ChurchApiKeyRepository repository) {
        this.repository = repository;
    }

    public Optional<String> getChurchIdForValidKey(String apiKey) {
        return repository.findByApiKey(apiKey)
                .filter(ChurchApiKeyEntity::isActive)
                .map(ChurchApiKeyEntity::getChurchId);
    }
}
