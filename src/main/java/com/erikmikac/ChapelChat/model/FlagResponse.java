package com.erikmikac.ChapelChat.model;

/**
 * Represents a response when a user's request is flagged for some reason,
 * such as rate limiting or potential prompt injection.
 *
 * @param flagResaon  The reason why the request was flagged.
 * @param botResponse The response to be sent to the user.
 */
public record FlagResponse (String flagResaon, String botResponse) {}