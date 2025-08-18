package com.erikmikac.ChapelChat.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_log_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatLogMetrics {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chat_log_id", nullable = false)
    private UUID chatLogId;

    @Column(name = "church_id", nullable = false)
    private String churchId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "request_tokens")
    private Integer requestTokens;

    @Column(name = "response_tokens")
    private Integer responseTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    private String model;

    @Column(name = "cost_usd", precision = 10, scale = 5)
    private BigDecimal costUsd;

    private boolean flagged;
}
