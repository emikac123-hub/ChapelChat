package com.erikmikac.ChapelChat.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.entity.ChurchApiKeyEntity;
import com.erikmikac.ChapelChat.repository.ChurchApiKeyRepository;

class ChurchApiKeyServiceTest {

    private ChurchApiKeyRepository repository;
    private ChurchApiKeyService service;

    @BeforeEach
    void setUp() {
        repository = mock(ChurchApiKeyRepository.class);
        service = new ChurchApiKeyService(repository);
    }

    @Test
    void returnsChurchIdIfKeyIsValidAndActive() {
        Church church = new Church();
        church.setId("hope-baptist");
        ChurchApiKeyEntity entity = new ChurchApiKeyEntity();
        entity.setApiKey("valid-key");
        entity.setChurch(church);
        entity.setActive(true);

        when(repository.findByApiKey("valid-key")).thenReturn(Optional.of(entity));

        Optional<String> result = service.getChurchIdForValidKey("valid-key");

        assertTrue(result.isPresent());
        assertEquals("hope-baptist", result.get());
    }

    @Test
    void returnsEmptyIfKeyIsNotFound() {
        when(repository.findByApiKey("missing-key")).thenReturn(Optional.empty());

        Optional<String> result = service.getChurchIdForValidKey("missing-key");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyIfKeyIsRevoked() {
        ChurchApiKeyEntity entity = new ChurchApiKeyEntity();
        Church church = new Church();
        church.setId("grace-orthodox");
        entity.setApiKey("revoked-key");
        entity.setChurch(church);
        entity.setActive(false);

        when(repository.findByApiKey("revoked-key")).thenReturn(Optional.of(entity));

        Optional<String> result = service.getChurchIdForValidKey("revoked-key");

        assertTrue(result.isEmpty());
    }
}
