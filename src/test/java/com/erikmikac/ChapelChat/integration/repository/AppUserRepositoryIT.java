package com.erikmikac.ChapelChat.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.erikmikac.ChapelChat.entity.AppUser;
import com.erikmikac.ChapelChat.repository.AppUserRepository;

@DataJpaTest
class AppUserRepositoryIT extends BaseJpaIT {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private AppUserRepository repo;

    private AppUser userErik;
    private AppUser userEmma;

    @BeforeEach
    void seed() {
        // Seed user 1
        userErik = new AppUser();
        userErik.setUsername("erik");
        // If your AppUser has required fields, set them here:
        // userErik.setEmail("erik@example.com");
        // userErik.setDisplayName("Erik");
        // userErik.setPasswordHash("$2a$10$example");
        // userErik.setIsActive(true);
        em.persist(userErik);

        // Seed user 2
        userEmma = new AppUser();
        userEmma.setUsername("sara");
        // userEmma.setEmail("emma@example.com");
        // userEmma.setDisplayName("Sara");
        // userEmma.setPasswordHash("$2a$10$example");
        // userEmma.setIsActive(true);
        em.persist(userEmma);

        em.flush();
        em.clear();
    }

    @Test
    void findByUsername_returnsMatch_whenPresent() {
        var found = repo.findByUsername("erik");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("erik");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotPresent() {
        var notFound = repo.findByUsername("does-not-exist");
        assertThat(notFound).isEmpty();
    }

    /**
     * TODO: When creating the persist method, enforce uniqueness. 
     */
    // @Test
    // void uniqueConstraint_assumedOnUsername() {
    //     AppUser dup = new AppUser();
    //     dup.setUsername("erik");
    //     assertThatThrownBy(() -> {
    //         em.persistAndFlush(dup);
    //     }).isInstanceOf(Exception.class);
    // }
}
