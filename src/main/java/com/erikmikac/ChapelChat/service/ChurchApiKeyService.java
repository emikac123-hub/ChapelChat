package com.erikmikac.ChapelChat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.entity.ChurchApiKeyEntity;
import com.erikmikac.ChapelChat.repository.ChurchApiKeyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChurchApiKeyService {

    private final ChurchApiKeyRepository repository;

    public Optional<String> getChurchIdForValidKey(String apiKey) {
        return repository.findByApiKey(apiKey)
                .filter(x -> x.getIsActive())
                .map(x -> x.getChurch().getId());
    }

    public Optional<ChurchApiKeyEntity> getActiveChurchesByApiKey(String apiKey) {
        return repository.findByApiKey(apiKey)
                .filter(x -> x.getIsActive());
    }
}
