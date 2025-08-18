package com.erikmikac.ChapelChat.model.admin;

/**
 * Represents the result of resolving a presented API key.
 * It contains the key's ID and the associated church ID.
 *
 * @param id       The unique identifier of the API key.
 * @param churchId The ID of the church associated with the key.
 */
public record ResolvedKey(String id, String churchId) {}