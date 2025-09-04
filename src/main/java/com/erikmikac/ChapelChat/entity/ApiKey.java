package com.erikmikac.ChapelChat.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "api_keys", uniqueConstraints = @UniqueConstraint(name = "uq_api_keys_token_hash", columnNames = "token_hash"), indexes = {
        @Index(name = "idx_api_keys_church_revoked", columnList = "org_id, revoked_at"),
        @Index(name = "idx_api_keys_last_used_at", columnList = "last_used_at")
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ApiKey {

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false) // FK → org.id
    @ToString.Exclude
    private Organization organization;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "token_prefix", length = 32)
    private String tokenPrefix;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @PrePersist
    void prePersist() {
        if (id == null)
            id = UUID.randomUUID(); // ok if you also have DB default
        if (createdAt == null)
            createdAt = OffsetDateTime.now();
    }

    // ----- Convenience (not mapped) -----
    @Transient
    public String getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    /** Allow setting the FK by id without loading Church from DB. */
    public void setChurchId(String churchId) {
        if (churchId == null) {
            this.organization = null;
            return;
        }
        Organization stub = new Organization();
        stub.setId(churchId);
        this.organization = stub;
    }
}