package com.erikmikac.ChapelChat.tenant;

public interface HasOrgIdentity {
    String getOrgId();             // preferred
    default String getOrganizationId() { return null; } // legacy compat
    default String getChurchId() { return null; }       // legacy compat
    String getTenantId();          // optional
    String getOrgType();           // e.g., "CHURCH", "SMB"
}
