package com.erikmikac.ChapelChat.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erikmikac.ChapelChat.entity.ChatLog;

public interface ChatLogRepository extends JpaRepository<ChatLog, UUID> {

        @Query(value = """
                        SELECT * FROM chat_logs
                        WHERE metadata ->> 'topic' = :topic
                        AND (metadata ->> 'confidence')::float > :minConfidence
                        """, nativeQuery = true)
        List<ChatLog> findHighConfidenceByTopic(@Param("topic") String topic,
                        @Param("minConfidence") float minConfidence);

        @Query(value = """
                        SELECT * FROM chat_logs
                        WHERE metadata ->> 'flagged' = 'true'
                        """, nativeQuery = true)
        List<ChatLog> findFlaggedChats();

        int countBySessionId(UUID sessionId);

        int countBySourceIpAndTimestampAfter(String ip, Instant windowStart);
        int deleteByTimestampBefore(Instant cutoff);
}
