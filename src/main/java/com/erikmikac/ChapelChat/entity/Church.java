package com.erikmikac.ChapelChat.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Church {
    @Id
    private String id; // e.g. "grace-orthodox"

    private String name;

    private String allowedOrigin;

    @OneToMany(mappedBy = "church")
    private List<AppUser> users;

    @OneToOne(mappedBy = "church")
    private ChurchApiKeyEntity apiKey;

}