package com.erikmikac.ChapelChat.enums;

public enum OrgType {
    CHURCH,
    SMB,
    NONPROFIT,
    EDUCATION;

    /** Normalize from string input (e.g., from DB). */
    public static OrgType fromString(String value) {
        if (value == null) return null;
        try {
            return OrgType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // or throw, depending on your tolerance
        }
    }
}
