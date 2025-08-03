package com.erikmikac.ChapelChat.enums;

public enum ChatLogMetadataKey {
    INTENT("intent"),
    TONE("tone"),
    TOPIC("topic"),
    FLAGGED("flagged"),
    FLAG_REASON("flagReason"),
    REJECTION_REASON("rejectionReason"),
    PROMPT_INJECTION("promptInjection"),
    OPENAI_MODEL("model"),
    TEMPERATURE("temperature"),
    USER_AGENT("user-agent");

    private final String key;

    ChatLogMetadataKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
