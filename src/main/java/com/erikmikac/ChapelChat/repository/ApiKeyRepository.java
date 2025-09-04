package com.erikmikac.ChapelChat.repository;

import java.util.List;

import com.erikmikac.ChapelChat.entity.ApiKey;

public interface ApiKeyRepository
    extends org.springframework.data.jpa.repository.JpaRepository<ApiKey, java.util.UUID> {
  java.util.Optional<ApiKey> findByTokenHashAndRevokedAtIsNull(String tokenHash);

  List<ApiKey> findByOrganization_IdAndRevokedAtIsNullOrderByCreatedAtDesc(String orgId);

}
