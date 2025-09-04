package com.erikmikac.ChapelChat.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true) // keep it safe
public class Organization {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    private String name;
    private String allowedOrigin;

    @OneToMany(mappedBy = "organization")
    @ToString.Exclude
    private Set<ApiKey> apiKeys = new HashSet<>();

 
    @OneToMany(mappedBy = "organization")
    private Set<AppUser> users = new HashSet<>();
}
