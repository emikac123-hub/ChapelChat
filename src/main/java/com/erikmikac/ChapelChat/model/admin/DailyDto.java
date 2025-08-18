package com.erikmikac.ChapelChat.model.admin;

import java.math.BigDecimal;
/**
 * Daily aggregates for time-series charts (one row per day).
 * Returned by /admin/analytics/timeseries.
 *
 * @param day           ISO date (YYYY-MM-DD) representing the bucket
 * @param conversations number of conversations that day
 * @param tokens        total tokens that day
 * @param cost_usd      estimated cost that day
 */
public record DailyDto(String day, long conversations, long tokens, BigDecimal cost_usd) {}