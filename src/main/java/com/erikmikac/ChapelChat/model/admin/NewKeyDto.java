package com.erikmikac.ChapelChat.model.admin;
/**
 * One-time response when creating a new API key.
 * The plaintext is shown once to the admin and never stored in the database.
 *
 * @param id         key identifier
 * @param plaintext  the full secret value (display once, then discard)
 */
public record NewKeyDto(String id, String plaintext) {
}