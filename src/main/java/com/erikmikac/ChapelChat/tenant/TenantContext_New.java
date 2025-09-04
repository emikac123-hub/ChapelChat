package com.erikmikac.ChapelChat.tenant;

import java.util.Objects;

public final class TenantContext_New {

    // Thread-local container for all per-request tenant info
    private static final ThreadLocal<Context> CTX = new ThreadLocal<>();

    /** Lightweight value object for tenant context */
    public static final class Context {
        private final String orgId;      // replaces churchId
        private final String tenantId;   // optional: if you segment per site/brand
        private final OrgType orgType;   // CHURCH, SMB, etc.

        public Context(String orgId, String tenantId, OrgType orgType) {
            this.orgId = orgId;
            this.tenantId = tenantId;
            this.orgType = orgType;
        }
        public String getOrgId()   { return orgId; }
        public String getTenantId(){ return tenantId; }
        public OrgType getOrgType(){ return orgType; }

        @Override public String toString() {
            return "Context{orgId=%s, tenantId=%s, orgType=%s}"
                .formatted(orgId, tenantId, orgType);
        }
        @Override public int hashCode() { return Objects.hash(orgId, tenantId, orgType); }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Context c)) return false;
            return Objects.equals(orgId, c.orgId)
                && Objects.equals(tenantId, c.tenantId)
                && orgType == c.orgType;
        }
    }

    public enum OrgType { CHURCH, SMB, NONPROFIT /* extend as needed */ }

    // ----- Core API -----
    public static void set(Context ctx) { CTX.set(ctx); }
    public static Context get()         { return CTX.get(); }
    public static void clear()          { CTX.remove(); }

    // Convenience accessors
    public static String getOrgId()     { return get() != null ? get().getOrgId() : null; }
    public static String getTenantId()  { return get() != null ? get().getTenantId() : null; }
    public static OrgType getOrgType()  { return get() != null ? get().getOrgType() : null; }

    // ----- Backward compatibility (remove once callers are migrated) -----
    @Deprecated public static void setChurchId(String id) {
        // keep previous behavior but store neutral fields
        set(new Context(id, /*tenantId*/ null, OrgType.CHURCH));
    }
    @Deprecated public static String getChurchId() { return getOrgId(); }

}