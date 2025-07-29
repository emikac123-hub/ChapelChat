package com.erikmikac.ChapelChat.security;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "api_keys")
@Data
public class ChurchApiKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "church_id", nullable = false)
    private String churchId;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;

}
