package com.erikmikac.ChapelChat.model.admin;
import java.math.BigDecimal;
/**
 * High-level KPI rollup for a date range and church tenant.
 * Returned by /admin/analytics/summary to power the dashboard KPI cards.
 *
 * @param conversations  total number of chat log entries in range
 * @param sessions       distinct session count in range
 * @param avg_latency_ms average end-to-end model latency (ms)
 * @param p95_latency_ms 95th percentile latency (ms)
 * @param tokens         total tokens used (request + response)
 * @param cost_usd       estimated model cost in USD
 * @param flagged_count  number of flagged interactions
 */
public record SummaryDto(long conversations, long sessions, int avg_latency_ms,
                         int p95_latency_ms, long tokens, BigDecimal cost_usd, long flagged_count) { }
