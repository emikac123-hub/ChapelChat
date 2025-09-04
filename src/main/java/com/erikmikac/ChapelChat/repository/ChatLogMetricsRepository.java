package com.erikmikac.ChapelChat.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erikmikac.ChapelChat.entity.ChatLogMetrics;

public interface ChatLogMetricsRepository extends JpaRepository<ChatLogMetrics, UUID> {
    List<ChatLogMetrics> findByOrgIdAndTimestampBetween(String orgId, Instant start, Instant end);
}