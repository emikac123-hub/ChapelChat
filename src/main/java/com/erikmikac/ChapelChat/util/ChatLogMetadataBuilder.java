package com.erikmikac.ChapelChat.util;

import java.util.HashMap;
import java.util.Map;

import com.erikmikac.ChapelChat.enums.ChatLogMetadataKey;

/**
 * A fluent builder for creating metadata maps for {@link com.erikmikac.ChapelChat.entity.ChatLog} entities.
 * <p>
 * This builder simplifies the process of constructing the metadata map by providing
 * a chainable, readable API. It ensures that keys from {@link ChatLogMetadataKey} are used
 * correctly and handles null-checking for optional values.
 *
 * <pre>{@code
 * Map<String, Object> metadata = ChatLogMetadataBuilder.create()
 *     .withFlagged(true, "rate_limit_exceeded")
 *     .withModel("gpt-4")
 *     .withTemperature(0.7)
 *     .build();
 * }</pre>
 */
public class ChatLogMetadataBuilder {

    private final Map<String, Object> metadata = new HashMap<>();

    private ChatLogMetadataBuilder() {}

    /**
     * Creates a new instance of the builder.
     *
     * @return A new {@link ChatLogMetadataBuilder} instance.
     */
    public static ChatLogMetadataBuilder create() {
        return new ChatLogMetadataBuilder();
    }

    /**
     * Sets the flagged status and the reason for flagging.
     *
     * @param flagged {@code true} if the chat log should be flagged, {@code false} otherwise.
     * @param reason The reason for flagging. This is only added if {@code flagged} is true
     *               and the reason is not null or blank.
     * @return The builder instance for chaining.
     */
    public ChatLogMetadataBuilder withFlagged(boolean flagged, String reason) {
        metadata.put(ChatLogMetadataKey.FLAGGED.key(), flagged);
        if (flagged && reason != null && !reason.isBlank()) {
            metadata.put(ChatLogMetadataKey.FLAG_REASON.key(), reason);
        }
        return this;
    }

    /**
     * Adds the checksum of the church profile used in the interaction.
     *
     * @param checksum The MD5 checksum of the church profile JSON.
     * @return The builder instance for chaining.
     */
    public ChatLogMetadataBuilder withProfileChecksum(String checksum) {
        if (checksum != null) {
            metadata.put(ChatLogMetadataKey.PROFILE_CHECKSUM.key(), checksum);
        }
        return this;
    }

    /**
     * Adds the OpenAI model used for the bot's response.
     *
     * @param model The name of the OpenAI model (e.g., "gpt-4").
     * @return The builder instance for chaining.
     */
    public ChatLogMetadataBuilder withModel(String model) {
        if (model != null) {
            metadata.put(ChatLogMetadataKey.OPENAI_MODEL.key(), model);
        }
        return this;
    }

    /**
     * Adds the temperature setting used for the OpenAI API call.
     *
     * @param temperature The temperature value (e.g., 0.7).
     * @return The builder instance for chaining.
     */
    public ChatLogMetadataBuilder withTemperature(Double temperature) {
        if (temperature != null) {
            metadata.put(ChatLogMetadataKey.TEMPERATURE.key(), temperature);
        }
        return this;
    }

    /**
     * Adds the User-Agent string from the client's request.
     *
     * @param userAgent The User-Agent string.
     * @return The builder instance for chaining.
     */
    public ChatLogMetadataBuilder withUserAgent(String userAgent) {
        if (userAgent != null && !userAgent.isBlank()) {
            metadata.put(ChatLogMetadataKey.USER_AGENT.key(), userAgent);
        }
        return this;
    }

        public ChatLogMetadataBuilder withOrgType(String orgType) {
        if (orgType != null && !orgType.isBlank()) {
            metadata.put(ChatLogMetadataKey.ORG_TYPE.key(), orgType);
        }
        return this;
    }
    /**
     * Constructs the final metadata map.
     *
     * @return A {@link Map} containing all the configured metadata.
     */
    public Map<String, Object> build() {
        return metadata;
    }
}
