package com.erikmikac.ChapelChat.exceptions;

public class OrganizationProfileNotFoundException extends Exception {

    public OrganizationProfileNotFoundException(String message) {
        super(message);
    }

    public OrganizationProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
