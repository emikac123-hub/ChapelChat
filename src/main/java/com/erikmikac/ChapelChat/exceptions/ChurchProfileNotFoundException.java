package com.erikmikac.ChapelChat.exceptions;

public class ChurchProfileNotFoundException extends Exception {

    public ChurchProfileNotFoundException(String message) {
        super(message);
    }

    public ChurchProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
