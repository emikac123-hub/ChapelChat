package com.erikmikac.ChapelChat.model.admin;

import java.math.BigDecimal;
/**
 * Aggregates grouped by model (e.g., gpt-4o, gpt-3.5-turbo) for breakdown charts.
 * Returned by /admin/analytics/model-split.
 *
 * @param model         model identifier
 * @param conversations number of conversations on this model
 * @param cost_usd      total cost attributed to this model
 */
public record ModelSplitDto(String model, long conversations, BigDecimal cost_usd) {}