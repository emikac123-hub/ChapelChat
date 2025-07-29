package com.erikmikac.ChapelChat.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.erikmikac.ChapelChat.repository.ChurchApiKeyRepository;
import com.erikmikac.ChapelChat.security.ChurchApiKeyEntity;

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
        ChurchApiKeyEntity entity = new ChurchApiKeyEntity();
        entity.setApiKey("valid-key");
        entity.setChurchId("hope-baptist");
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
        entity.setApiKey("revoked-key");
        entity.setChurchId("grace-orthodox");
        entity.setActive(false);

        when(repository.findByApiKey("revoked-key")).thenReturn(Optional.of(entity));

        Optional<String> result = service.getChurchIdForValidKey("revoked-key");

        assertTrue(result.isEmpty());
    }
}
