package com.erikmikac.ChapelChat.integration.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.erikmikac.ChapelChat.entity.ApiKey;
import com.erikmikac.ChapelChat.entity.Organization;
import com.erikmikac.ChapelChat.repository.ApiKeyRepository;

class ApiKeyRepositoryIT extends  BaseJpaIT{

  @Autowired private ApiKeyRepository repo;
  @Autowired private TestEntityManager em;

  private Organization churchA;
  private Organization churchB;

  @BeforeEach
  void seed() {
    churchA = new Organization();
    churchA.setId("church-A");
    churchA.setName("Church A");
    churchA.setAllowedOrigin("http://a.example");
    em.persist(churchA);

    churchB = new Organization();
    churchB.setId("church-B");
    churchB.setName("Church B");
    churchB.setAllowedOrigin("http://b.example");
    em.persist(churchB);

    ApiKey a1 = new ApiKey();
    a1.setId(UUID.randomUUID());
    a1.setTokenHash("hash-active-old");
    a1.setOrganization(churchA);
    a1.setCreatedAt(OffsetDateTime.parse("2025-08-01T10:00:00Z"));
    a1.setRevokedAt(null);
    em.persist(a1);

    ApiKey a2 = new ApiKey();
    a2.setId(UUID.randomUUID());
    a2.setTokenHash("hash-active-new");
    a2.setOrganization(churchA);
    a2.setCreatedAt(OffsetDateTime.parse("2025-08-01T10:00:00Z"));
    a2.setRevokedAt(null);
    em.persist(a2);

    ApiKey a3 = new ApiKey();
    a3.setId(UUID.randomUUID());
    a3.setTokenHash("hash-revoked");
    a3.setOrganization(churchA);
    a3.setCreatedAt(OffsetDateTime.parse("2025-08-01T10:00:00Z"));
    a3.setRevokedAt(OffsetDateTime.parse("2025-08-01T10:00:00Z"));
    em.persist(a3);

    ApiKey b1 = new ApiKey();
    b1.setId(UUID.randomUUID());
    b1.setTokenHash("hash-b1");
    b1.setOrganization(churchB);
    b1.setCreatedAt(OffsetDateTime.parse("2025-08-01T10:00:00Z"));
    b1.setRevokedAt(null);
    em.persist(b1);

    em.flush();
    em.clear();
  }

  @Test
  void findByTokenHashAndRevokedAtIsNull_returnsOnlyActive() {
    var active = repo.findByTokenHashAndRevokedAtIsNull("hash-active-old");
    assertThat(active).isPresent();
    assertThat(active.get().getRevokedAt()).isNull();

    var revoked = repo.findByTokenHashAndRevokedAtIsNull("hash-revoked");
    assertThat(revoked).isEmpty();

    var missing = repo.findByTokenHashAndRevokedAtIsNull("nope");
    assertThat(missing).isEmpty();
  }

  @Test
  void findByChurchIdAndRevokedAtIsNull_ordersByCreatedAtDesc_andFiltersChurch() {
    List<ApiKey> a = repo.findByOrganization_IdAndRevokedAtIsNullOrderByCreatedAtDesc("church-A");
    assertThat(a).extracting(ApiKey::getTokenHash)
        .containsExactly("hash-active-old", "hash-active-new");

    List<ApiKey> b = repo.findByOrganization_IdAndRevokedAtIsNullOrderByCreatedAtDesc("church-B");
    assertThat(b).extracting(ApiKey::getTokenHash)
        .containsExactly("hash-b1");
  }
}