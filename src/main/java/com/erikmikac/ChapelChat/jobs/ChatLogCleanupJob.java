package com.erikmikac.ChapelChat.jobs;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.erikmikac.ChapelChat.repository.ChatLogRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty("cleanup.enabled")
public class ChatLogCleanupJob {

    private final ChatLogRepository chatLogRepository;

    @Value("${chatlog.retention.days:90}")
    private int retentionDays;

    public ChatLogCleanupJob(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    @Scheduled(cron = "0 0 2 * * *") 
    public void deleteOldChatLogs() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        int deleted = chatLogRepository.deleteByTimestampBefore(cutoff);
        log.info("🧹 Deleted {} ChatLog entries older than {}", deleted, cutoff);
    }
}
