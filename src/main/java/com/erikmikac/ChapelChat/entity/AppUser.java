package com.erikmikac.ChapelChat.entity;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.erikmikac.ChapelChat.enums.Roles;
import com.erikmikac.ChapelChat.tenant.HasChurchId;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor
public class AppUser implements HasChurchId, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Map Spring Security "username" to your DB's email column
    @Column(name = "email", nullable = false, length = 255)
    private String username;

    // Map to password_hash column (no rename needed)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id")                  // add this column via Liquibase (below)
    private Church church;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<Roles> roles = new java.util.HashSet<>();

    @Override public String getChurchId() { return church != null ? church.getId() : null; }

    // --- UserDetails bridge ---
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) return java.util.List.of();
        var base = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        if (roles.contains(Roles.ADMIN) || roles.contains(Roles.OWNER)) {
            base.add(new SimpleGrantedAuthority("apikeys:rotate"));
            base.add(new SimpleGrantedAuthority("profile:write"));
        }
        return java.util.Collections.unmodifiableSet(base);
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
