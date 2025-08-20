package com.erikmikac.ChapelChat.integration.repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.erikmikac.ChapelChat.entity.ChatLog;
import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.model.FlagResponse;
import com.erikmikac.ChapelChat.repository.ChatLogRepository;
import com.erikmikac.ChapelChat.util.ChatLogMetadataBuilder;

@DataJpaTest
class ChatLogRepositoryIT extends BaseJpaIT {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ChatLogRepository repo;

    private ChatLog chatLog;
    private final UUID someOtherSessionId = UUID.fromString("281f38b5-f27c-4091-a6a8-dad375023501");
    private final UUID sessionId = UUID.fromString("281f38b5-f27c-4091-a6a8-dad375023522");
    private final String sourceIp = "127.0.0.1";
    private ChatLog flaggedChatLog;

    @BeforeEach
    void seed() {
        // Seed user 1
        Church c = new Church();
        c.setId("hope-baptist");
        c.setName("Hope Baptist");
        c.setAllowedOrigin("http://localhost");
        em.persist(c);
        final var meta = ChatLogMetadataBuilder.create()
                .withModel("turbo")
                .withTemperature(0.9)
                .withUserAgent("Chrome").build();
        chatLog = ChatLog.builder()
                .botResponse("Good idea!")
                .userQuestion("I'd like to start a church.")
                .churchId("hope-baptist")
                .sessionId(sessionId)
                .metadata(meta)
                .sourceIp(sourceIp)
                .timestamp(Instant.now())
                .build();

        em.persist(chatLog);
        em.flush();
        em.clear();
    }

    @Test
    void findAmountOfSessionIds() {
        var found = repo.countBySessionId(sessionId);
        assertEquals(found, 1);
    }

    @Test
    void findAmountSourceIp() {
        final var meta = ChatLogMetadataBuilder.create()
                .withModel("turbo")
                .withTemperature(0.9)
                .withUserAgent("Chrome").build();
        chatLog = ChatLog.builder()
                .botResponse("Good idea!")
                .userQuestion("I'd like to start a church.")
                .churchId("hope-baptist")
                .sessionId(someOtherSessionId)
                .metadata(meta)
                .sourceIp(sourceIp)
                .timestamp(Instant.now())
                .build();
        em.persist(chatLog);
        em.flush();
        em.clear();
        final var yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        final var count = repo.countBySourceIpAndTimestampAfter(sourceIp, yesterday);
        assertEquals(count, 2);
    }

    @Test
    void shouldFindFlaggedChatLogs() {
        final var flaggedMeta = ChatLogMetadataBuilder.create().withFlagged(true, "profanity").build();
        flaggedChatLog = ChatLog.builder()
                .botResponse("Good idea!")
                .userQuestion("I'd like to start a small business, darn it.")
                .churchId("hope-baptist")
                .sessionId(someOtherSessionId)
                .metadata(flaggedMeta)
                .sourceIp(sourceIp)
                .timestamp(Instant.now())
                .build();
        em.persist(flaggedChatLog);
        em.flush();
        em.clear();
        final var flaggedChats = repo.findFlaggedChats();
        assertNotNull(flaggedChats);
        assertEquals(1, flaggedChats.size());
        assertEquals(flaggedChatLog, flaggedChats.getFirst());

    }

    @Test
    void shouldDeleteAllChatlogs() {
        repo.deleteByTimestampBefore(Instant.now());
        em.flush();
        em.clear();
        final List<ChatLog> chatLog = repo.findAll();
        assertNotNull(chatLog);
        assertEquals(0, chatLog.size());
    }
}
