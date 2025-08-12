package com.erikmikac.ChapelChat.tenant;

public final class TenantContext {
    // Create a local thread specific to the tenant logged in.
    private static final ThreadLocal<String> CHURCH = new ThreadLocal<>();

    public static void setChurchId(String id) {
        CHURCH.set(id);
    }

    public static String getChurchId() {
        return CHURCH.get();
    }

    public static void clear() {
        CHURCH.remove();
    }

    private TenantContext() {
    }
}
