package com.erikmikac.ChapelChat.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskRequest {

    private String message;
    private UUID sessionId;
}
