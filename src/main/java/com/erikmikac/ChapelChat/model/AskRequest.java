package com.erikmikac.ChapelChat.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request from a user to the "ask" endpoint.
 * It contains the user's message and a session identifier.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskRequest {

    /** The message or question sent by the user. */
    private String message;

    /** A unique identifier for the user's session. */
    private UUID sessionId;
}
