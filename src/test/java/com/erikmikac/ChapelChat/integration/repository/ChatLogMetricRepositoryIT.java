package com.erikmikac.ChapelChat.integration.repository;

import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.entity.ChatLogMetrics;
import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.repository.ChatLogMetricsRepository;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;
import com.erikmikac.ChapelChat.repository.ChurchRepository;

@DataJpaTest
class ChatLogMetricRepositoryIT extends BaseJpaIT {

    @Autowired
    private ChatLogMetricsRepository metricsRepo;
    @Autowired
    private ChatLogRepository chatLogRepo;
    @Autowired
    private ChurchRepository churchRepo; // or use TestEntityManager/JdbcTemplate to insert church

    private final String churchId = "hope-baptist";
    private Instant now;
    private Instant yesterday;
    private Instant twoDaysAgo;

    @BeforeEach
    void seed() {
        now = Instant.now();
        yesterday = now.minusSeconds(24 * 60 * 60);
        twoDaysAgo = now.minusSeconds(2 * 24 * 60 * 60);

        // 1) Seed parent church (satisfy FK: chat_logs.church_id -> church.id)
        Church church = new Church();
        church.setId(churchId);
        church.setName("Hope Baptist");
        church.setAllowedOrigin("http://localhost");
        churchRepo.save(church);

        // 2) Seed chat log (satisfy FK for metrics: chat_log_metrics.chat_log_id ->
        // chat_logs.id)
        ChatLog chatlog = ChatLog.builder()
                .churchId(churchId)
                .sessionId(UUID.randomUUID())
                .userQuestion("Should I go to church?")
                .botResponse("Good idea!")
                .timestamp(now)
                .build();
        chatlog = chatLogRepo.saveAndFlush(chatlog); // ensure id is generated & row exists
        UUID chatLogId = chatlog.getId();

        // 3) Seed metrics: one inside window, one outside
        ChatLogMetrics m1 = ChatLogMetrics.builder()
                .chatLogId(chatLogId)
                .churchId(churchId)
                .timestamp(yesterday) // inside [from, to]
                .latencyMs(120)
                .requestTokens(100)
                .responseTokens(200)
                .totalTokens(300)
                .model("gpt-test")
                .costUsd(BigDecimal.valueOf(0.01)) // avoid float ctor
                .flagged(false)
                .build();

        ChatLogMetrics m2 = ChatLogMetrics.builder()
                .chatLogId(chatLogId)
                .churchId(churchId)
                .timestamp(twoDaysAgo) // outside [from, to]
                .latencyMs(250)
                .requestTokens(50)
                .responseTokens(50)
                .totalTokens(100)
                .model("gpt-test")
                .costUsd(BigDecimal.valueOf(0.05))
                .flagged(true)
                .build();

        metricsRepo.saveAll(List.of(m1, m2));
        metricsRepo.flush(); // optional, ensures visible immediately
    }

    @Test
    void findByChurchIdAndTimestampBetween_returnsOnlyWithinRange() {
        Instant from = yesterday.minusSeconds(60);
        Instant to = now.plusSeconds(60);

        var results = metricsRepo.findByChurchIdAndTimestampBetween(churchId, from, to);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getChurchId()).isEqualTo(churchId);
        assertThat(results.get(0).getTimestamp()).isBetween(from, to);
    }
}