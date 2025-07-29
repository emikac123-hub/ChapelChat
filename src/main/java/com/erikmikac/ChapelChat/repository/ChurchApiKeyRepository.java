package com.erikmikac.ChapelChat.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erikmikac.ChapelChat.security.ChurchApiKeyEntity;

public interface ChurchApiKeyRepository extends JpaRepository<ChurchApiKeyEntity, UUID> {
    Optional<ChurchApiKeyEntity> findByApiKey(String apiKey);
}