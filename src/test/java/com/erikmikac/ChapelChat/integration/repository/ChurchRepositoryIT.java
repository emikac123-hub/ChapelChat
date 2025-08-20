package com.erikmikac.ChapelChat.integration.repository;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.erikmikac.ChapelChat.entity.ApiKey;
import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.repository.ChurchRepository;

public class ChurchRepositoryIT extends BaseJpaIT{

    @Autowired ChurchRepository repo;

    @BeforeEach
    void setup() {
        // seed data
        final var church1 = new Church();
        church1.setAllowedOrigin("localhost");
        church1.setApiKeys(Set.of(new ApiKey()));
        church1.setId("hope-baptist");
        church1.setName("Hope Baptist");
        
        repo.saveAndFlush(church1);

    }


    
}
