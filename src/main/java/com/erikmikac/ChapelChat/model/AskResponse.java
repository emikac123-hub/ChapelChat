package com.erikmikac.ChapelChat.model;

import lombok.Data;

@Data
public class AskResponse {
    private String response;

    public AskResponse(final String response) {
        this.response = response;
    }
}
