package com.erikmikac.ChapelChat.integration.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.entity.ChatLogMetrics;
import com.erikmikac.ChapelChat.entity.Organization;
import com.erikmikac.ChapelChat.repository.ChatLogMetricsRepository;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;
import com.erikmikac.ChapelChat.repository.OrganizationRepository;

@DataJpaTest
class ForeignKeysIntegrityIT extends BaseJpaIT {

  @Autowired OrganizationRepository churchRepo;
  @Autowired ChatLogRepository chatLogRepo;
  @Autowired ChatLogMetricsRepository metricsRepo;

  // 1) chat_logs.org_id must reference church(id)
  @Test
  void insertingChatLog_withoutExistingChurch_failsFK() {
    ChatLog log = ChatLog.builder()
        .orgId("missing-church")          // no parent row
        .userQuestion("Q")
        .botResponse("A")
        .timestamp(Instant.now())
        .build();

    assertThatThrownBy(() -> chatLogRepo.saveAndFlush(log))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasCauseInstanceOf(ConstraintViolationException.class)
        .satisfies(ex -> {
          var cve = (ConstraintViolationException) ex.getCause();
          assertThat(cve.getSQLException().getSQLState()).isEqualTo("23503"); // FK violation
          // Optional: assert constraint name if you want
          assertThat(cve.getConstraintName()).contains("fk_chat_logs_church");
        });
  }

  // 2) chat_log_metrics.chat_log_id must reference chat_logs(id)
  @Test
  void insertingMetrics_withNonexistentChatLogId_failsFK() {
    // Seed required church + valid chat_log (to show contrast later)
    Organization church = new Organization();
    church.setId("hope-baptist");
    church.setName("Hope Baptist");
    churchRepo.saveAndFlush(church);

    // Try metrics pointing to a random (non-existent) chat_log_id
    ChatLogMetrics badMetrics = ChatLogMetrics.builder()
        .orgId("hope-baptist")
        .chatLogId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
        .timestamp(Instant.now())
        .latencyMs(100)
        .totalTokens(123)
        .costUsd(new BigDecimal("0.01000"))
        .flagged(false)
        .build();

    assertThatThrownBy(() -> metricsRepo.saveAndFlush(badMetrics))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasCauseInstanceOf(ConstraintViolationException.class)
        .satisfies(ex -> {
          var cve = (ConstraintViolationException) ex.getCause();
          assertThat(cve.getSQLException().getSQLState()).isEqualTo("23503"); // FK violation
          assertThat(cve.getConstraintName()).contains("fk_chat_log_metrics_chat_logs");
        });
  }


}