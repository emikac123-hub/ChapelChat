package com.erikmikac.ChapelChat.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
@Entity
@Table(name = "chat_logs")
@AllArgsConstructor
public class ChatLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "church_id", nullable = false)
    private String churchId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "user_question", nullable = false, columnDefinition = "TEXT")
    private String userQuestion;

    @Column(name = "bot_response", nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    // Constructors
    public ChatLog() {
    }

    public ChatLog(String churchId, UUID sessionId, String userQuestion, String botResponse,
            String sourceIp, String userAgent, Map<String, Object> metadata) {
        this.churchId = churchId;
        this.sessionId = sessionId;
        this.userQuestion = userQuestion;
        this.botResponse = botResponse;
        this.sourceIp = sourceIp;
        this.userAgent = userAgent;
        this.metadata = metadata;
    }

    // Getters and setters omitted for brevity
}