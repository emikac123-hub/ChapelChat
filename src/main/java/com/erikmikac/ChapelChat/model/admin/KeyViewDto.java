package com.erikmikac.ChapelChat.model.admin;

public record KeyViewDto(String id, String masked, java.time.OffsetDateTime createdAt,
        java.time.OffsetDateTime lastUsedAt) {
}