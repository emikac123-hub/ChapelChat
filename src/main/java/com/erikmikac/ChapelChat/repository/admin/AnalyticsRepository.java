package com.erikmikac.ChapelChat.repository.admin;

import java.time.OffsetDateTime;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.erikmikac.ChapelChat.model.admin.SummaryDto;
import com.erikmikac.ChapelChat.tenant.TenantContext;

@Repository
public class AnalyticsRepository {
  private final NamedParameterJdbcTemplate jdbc;

  public AnalyticsRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final String SUMMARY_SQL = """
      WITH base AS (
        SELECT m.latency_ms, m.total_tokens, m.cost_usd, m.flagged, m."timestamp", l.session_id
        FROM chat_log_metrics m
        JOIN chat_logs l ON l.id = m.chat_log_id
        WHERE l.org_id = :churchId AND m."timestamp" >= :from AND m."timestamp" < :to
      )
      SELECT
        COUNT(*) AS conversations,
        COUNT(DISTINCT session_id) AS sessions,
        COALESCE(ROUND(AVG(latency_ms))::int,0) AS avg_latency_ms,
        COALESCE((PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY latency_ms))::int,0) AS p95_latency_ms,
        COALESCE(SUM(total_tokens),0) AS tokens,
        COALESCE(SUM(cost_usd),0)::numeric(12,4) AS cost_usd,
        COALESCE(SUM(CASE WHEN flagged THEN 1 ELSE 0 END),0) AS flagged_count
      FROM base
      """;

  public SummaryDto fetchSummary(OffsetDateTime from, OffsetDateTime to) {

    var churchId = TenantContext.getChurchId();
    var p = new MapSqlParameterSource().addValue("churchId", churchId).addValue("from", from).addValue("to", to);

    return jdbc.queryForObject(SUMMARY_SQL, p, (rs, i) -> new SummaryDto(
        rs.getLong("conversations"),
        rs.getLong("sessions"),
        rs.getInt("avg_latency_ms"),
        rs.getInt("p95_latency_ms"),
        rs.getLong("tokens"),
        rs.getBigDecimal("cost_usd"),
        rs.getLong("flagged_count")));
  }

}
