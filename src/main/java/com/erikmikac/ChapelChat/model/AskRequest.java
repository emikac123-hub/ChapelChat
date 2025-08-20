package com.erikmikac.ChapelChat.model;

import java.util.UUID;

import lombok.Data;

/**
 * Represents a request from a user to the "ask" endpoint.
 * It contains the user's message and a session identifier.
 */
@Data
public class AskRequest {

    /** The message or question sent by the user. */
    private String message;

    /** A unique identifier for the user's session. */
    private UUID sessionId;
}
