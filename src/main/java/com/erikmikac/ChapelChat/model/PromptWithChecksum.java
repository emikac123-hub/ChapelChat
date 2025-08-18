package com.erikmikac.ChapelChat.model;

/**
 * A container for the system prompt and the checksum of the profile it was generated from.
 *
 * @param systemPrompt    The system prompt to be used for the AI model.
 * @param profileChecksum The checksum of the church profile.
 */
public record PromptWithChecksum(String systemPrompt, String profileChecksum) {}
