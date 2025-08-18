package com.erikmikac.ChapelChat.repository;

import java.util.List;

import com.erikmikac.ChapelChat.entity.ApiKey;

// Repository (adjust package/type names to your project)
public interface ApiKeyRepository
    extends org.springframework.data.jpa.repository.JpaRepository<ApiKey, java.util.UUID> {
  java.util.Optional<ApiKey> findByTokenHashAndRevokedAtIsNull(String tokenHash);

  List<ApiKey> findByChurch_IdAndRevokedAtIsNullOrderByCreatedAtDesc(String churchId);

}
