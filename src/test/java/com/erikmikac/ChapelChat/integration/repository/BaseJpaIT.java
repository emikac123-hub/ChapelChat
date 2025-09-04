package com.erikmikac.ChapelChat.integration.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest   
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class BaseJpaIT {

  @Container
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("chapeldb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", POSTGRES::getUsername);
    r.add("spring.datasource.password", POSTGRES::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // Liquibase drives schema
    // Only if you don’t use Boot’s default changelog path:
    // r.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
    r.add("spring.liquibase.enabled", () -> "true");
  }

/**
 * Steps to writing integration tests
 * 1. Extend Base Jpa IT. This registers dynamic props and creates the docker container for the tests to run on.
 * 2. Create a new java file for your tests to run in. <repo-name>IT.java
 * 3. Seed data for your tests
 * 4. Test the queries on your seeded data.
 */
  
}