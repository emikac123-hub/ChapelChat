package com.erikmikac.ChapelChat.repository;

import com.erikmikac.ChapelChat.entity.ApiKey;

// Repository (adjust package/type names to your project)
public interface ApiKeyRepository extends org.springframework.data.jpa.repository.JpaRepository<ApiKey, java.util.UUID> {
  java.util.Optional<ApiKey> findByTokenHashAndRevokedAtIsNull(String tokenHash);
  java.util.List<ApiKey> findByChurchIdAndRevokedAtIsNullOrderByCreatedAtDesc(String churchId);
}
