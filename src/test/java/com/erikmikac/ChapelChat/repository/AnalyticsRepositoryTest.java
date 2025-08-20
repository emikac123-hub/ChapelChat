package com.erikmikac.ChapelChat.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.erikmikac.ChapelChat.model.admin.SummaryDto;
import com.erikmikac.ChapelChat.repository.admin.AnalyticsRepository;
import com.erikmikac.ChapelChat.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class AnalyticsRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbc;

    @InjectMocks
    private AnalyticsRepository repository;

    @Test
    void fetchSummary_returnsMappedDto_andPassesCorrectParams() throws Exception {
        // Arrange
        String churchId = "church-123";
        OffsetDateTime from = OffsetDateTime.parse("2025-08-01T00:00:00Z");
        OffsetDateTime to   = OffsetDateTime.parse("2025-08-02T00:00:00Z");

        // Mock TenantContext.getChurchId()
        try (MockedStatic<TenantContext> mockedTenant = Mockito.mockStatic(TenantContext.class)) {
            mockedTenant.when(TenantContext::getChurchId).thenReturn(churchId);

            // Capture args for verification and simulate RowMapper execution
            ArgumentCaptor<MapSqlParameterSource> paramsCap = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            ArgumentCaptor<RowMapper<SummaryDto>> mapperCap = ArgumentCaptor.forClass((Class) RowMapper.class);

            when(jdbc.queryForObject(anyString(), paramsCap.capture(), mapperCap.capture()))
                    .thenAnswer(inv -> {
                        RowMapper<SummaryDto> mapper = mapperCap.getValue();
                        ResultSet rs = mock(ResultSet.class);

                        when(rs.getLong("conversations")).thenReturn(42L);
                        when(rs.getLong("sessions")).thenReturn(7L);
                        when(rs.getInt("avg_latency_ms")).thenReturn(123);
                        when(rs.getInt("p95_latency_ms")).thenReturn(456);
                        when(rs.getLong("tokens")).thenReturn(9876L);
                        when(rs.getBigDecimal("cost_usd")).thenReturn(new BigDecimal("12.3456"));
                        when(rs.getLong("flagged_count")).thenReturn(3L);

                        return mapper.mapRow(rs, 0);
                    });

            // Act
            SummaryDto dto = repository.fetchSummary(from, to);

            // Assert: mapping
            assertNotNull(dto);
            assertEquals(42L, dto.conversations());
            assertEquals(7L, dto.sessions());
            assertEquals(123, dto.avg_latency_ms());
            assertEquals(456, dto.p95_latency_ms());
            assertEquals(9876L, dto.tokens());
            assertEquals(new BigDecimal("12.3456"), dto.cost_usd());
            assertEquals(3L, dto.flagged_count());

            // Assert: SQL + params
            MapSqlParameterSource ps = paramsCap.getValue();
            assertEquals(churchId, ps.getValue("churchId"));
            assertEquals(from, ps.getValue("from"));
            assertEquals(to, ps.getValue("to"));

            // Also ensure the expected SQL string (optional but nice)
            verify(jdbc).queryForObject(
                    argThat(sql -> sql.contains("WITH base AS")
                            && sql.contains("FROM chat_log_metrics m")
                            && sql.contains("JOIN chat_logs l ON l.id = m.chat_log_id")
                            && sql.contains("WHERE l.church_id = :churchId")
                            && sql.contains("COUNT(*) AS conversations")),
                    any(MapSqlParameterSource.class),
                    any(RowMapper.class)
            );
        }
    }

    @Test
    void fetchSummary_propagatesEmptyResult() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenant = Mockito.mockStatic(TenantContext.class)) {
            mockedTenant.when(TenantContext::getChurchId).thenReturn("church-123");

            when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                    .thenThrow(new EmptyResultDataAccessException(1));

            // Act + Assert
            assertThrows(
                    EmptyResultDataAccessException.class,
                    () -> repository.fetchSummary(OffsetDateTime.now().minusDays(1), OffsetDateTime.now())
            );
        }
    }
}