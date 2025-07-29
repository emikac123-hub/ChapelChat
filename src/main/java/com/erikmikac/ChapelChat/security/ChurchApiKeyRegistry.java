package com.erikmikac.ChapelChat.security;

import java.util.Map;

import lombok.Data;

public class ChurchApiKeyRegistry {

    private static final Map<String, ChurchKey> keys = Map.of(
            "test-api-key", new ChurchKey("hope-baptist", true),
            "grace-key", new ChurchKey("grace-orthodox", false) // revoked
    );

    public static ChurchKey get(String key) {
        return keys.get(key);
    }

    @Data
    public static class ChurchKey {
        public final String churchId;
        public final boolean active;

        public ChurchKey(String churchId, boolean active) {
            this.churchId = churchId;
            this.active = active;
        }
    }
}