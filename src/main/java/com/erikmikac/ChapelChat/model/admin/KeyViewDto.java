package com.erikmikac.ChapelChat.model.admin;

/**
 * A data transfer object for viewing API key information in a list.
 *
 * @param id         The unique identifier of the API key.
 * @param masked     A masked version of the API key (e.g., showing only the last few characters).
 * @param createdAt  The timestamp when the key was created.
 * @param lastUsedAt The timestamp when the key was last used, or null if never used.
 */
public record KeyViewDto(String id, String masked, java.time.OffsetDateTime createdAt,
        java.time.OffsetDateTime lastUsedAt) {
}