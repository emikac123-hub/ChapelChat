package com.erikmikac.ChapelChat.model.admin;
/**
 * Row for “slowest responses” tables used in deep analytics.
 * Returned by /admin/analytics/slowest.
 *
 * @param chat_log_id   primary key of the chat log entry
 * @param latency_ms    end-to-end model latency in milliseconds
 * @param total_tokens  total tokens for the exchange
 * @param model         model used for the response
 * @param user_question truncated/plaintext user prompt for quick triage
 * @param timestamp     ISO-8601 timestamp of the interaction
 */
public record SlowRowDto(String chat_log_id, int latency_ms, long total_tokens, String model, String user_question, String timestamp) {}