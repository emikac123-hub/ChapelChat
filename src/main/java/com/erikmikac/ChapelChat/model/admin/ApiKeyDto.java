package com.erikmikac.ChapelChat.model.admin;
/**
 * List view item for API keys in the admin UI.
 * Returned by GET /admin/api-keys; key material is masked.
 *
 * @param id           stable identifier (not the secret)
 * @param masked       masked preview (e.g., sk_live_****abcd)
 * @param created_at   ISO-8601 creation timestamp
 * @param last_used_at ISO-8601 last-used timestamp or null if never used
 */
public record ApiKeyDto(String id, String masked, String created_at, String last_used_at) {
}
