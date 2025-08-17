package com.erikmikac.ChapelChat.service.admin;

import org.springframework.beans.factory.annotation.Value;

import com.erikmikac.ChapelChat.entity.ApiKey;
import com.erikmikac.ChapelChat.model.admin.KeyViewDto;
import com.erikmikac.ChapelChat.model.admin.NewKeyDto;
import com.erikmikac.ChapelChat.model.admin.ResolvedKey;
import com.erikmikac.ChapelChat.repository.ApiKeyRepository;;
// Service
@org.springframework.stereotype.Service
public class ApiKeyService {

  private static final int RAW_BYTES = 32; // 256-bit secret (~43 base64url chars)
  private static final String PREFIX = "ccak_"; // ChapelChat Api Key (pick any)
  private static final java.time.Duration LAST_USED_WRITE_THROTTLE = java.time.Duration.ofMinutes(1);

  private final ApiKeyRepository repo;
  private final java.security.SecureRandom rng = new java.security.SecureRandom();
  private final String pepper; // optional server-side secret to HMAC the hash

  public ApiKeyService(ApiKeyRepository repo,
                       @Value("${chapelchat.apikey.pepper:}") String pepper) {
    this.repo = repo;
    this.pepper = pepper; // keep empty if you don’t want peppering
  }

  /** Create a new API key, store only the hash, return plaintext once. */
  @org.springframework.transaction.annotation.Transactional
  public NewKeyDto createKey(String churchId, String createdBy) {
    var raw = PREFIX + base64Url(randomBytes(RAW_BYTES));
    var hash = hashKey(raw, pepper);

    var entity = new ApiKey();

    entity.setId(java.util.UUID.randomUUID());
    entity.setChurchId(churchId);
    entity.setTokenHash(hash);
    entity.setCreatedAt(java.time.OffsetDateTime.now());
    entity.setCreatedBy(createdBy);
    // lastUsedAt null; revokedAt null

    repo.save(entity);
    return new NewKeyDto(entity.getId().toString(), raw); // show plaintext ONCE
  }

  /** Revoke (soft delete) an API key by id for the current tenant. */
  @org.springframework.transaction.annotation.Transactional
  public void revokeKey(java.util.UUID id, String churchId) {
    var key = repo.findById(id).orElseThrow();
    if (!key.getChurchId().equals(churchId)) {
      throw new org.springframework.security.access.AccessDeniedException("Cross-tenant revoke");
    }
    if (key.getRevokedAt() == null) {
      key.setRevokedAt(java.time.OffsetDateTime.now());
      repo.save(key);
    }
  }

  /** List keys for the current tenant (masked). */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public java.util.List<KeyViewDto> listKeys(String churchId) {
    return repo.findByChurchIdAndRevokedAtIsNullOrderByCreatedAtDesc(churchId).stream()
        .map(k -> new KeyViewDto(
            k.getId().toString(),
            mask(k.getTokenHash()), // we don’t have plaintext; mask by hash fingerprint
            k.getCreatedAt(),
            k.getLastUsedAt()))
        .toList();
  }

  /**
   * Resolve a presented plaintext key into (id, churchId), or return null if invalid/revoked.
   * Also updates last_used_at, with a throttle to avoid a write per request.
   */
  @org.springframework.transaction.annotation.Transactional
  public ResolvedKey resolve(String presentedPlaintext) {
    if (presentedPlaintext == null || presentedPlaintext.isBlank()) return null;
    var hash = hashKey(presentedPlaintext, pepper);
    var found = repo.findByTokenHashAndRevokedAtIsNull(hash).orElse(null);
    if (found == null) return null;

    // Throttle last_used_at writes to at most once per minute
    var now = java.time.OffsetDateTime.now();
    if (found.getLastUsedAt() == null || now.isAfter(found.getLastUsedAt().plus(LAST_USED_WRITE_THROTTLE))) {
      found.setLastUsedAt(now);
      repo.save(found);
    }
    return new ResolvedKey(found.getId().toString(), found.getChurchId());
  }

  // ----- helpers -----

  private static byte[] randomBytes(int n) {
    var bytes = new byte[n];
    new java.security.SecureRandom().nextBytes(bytes);
    return bytes;
  }

  private static String base64Url(byte[] b) {
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  /** Hash with SHA-256; if a server-side pepper is configured, use HMAC-SHA256(pepper, key). */
  private static String hashKey(String raw, String pepper) {
    try {
      byte[] digest;
      if (pepper != null && !pepper.isBlank()) {
        var mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(pepper.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        digest = mac.doFinal(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      } else {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        digest = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      }
      return java.util.HexFormat.of().formatHex(digest); // hex string
    } catch (Exception e) {
      throw new IllegalStateException("Hashing error", e);
    }
  }

  private static String mask(String tokenHashHex) {
    // Show a short fingerprint from the hash so admins can recognize the key
    if (tokenHashHex == null || tokenHashHex.length() < 8) return "****";
    var tail = tokenHashHex.substring(tokenHashHex.length() - 8);
    return "sk_****" + tail;
  }

  /** Optional: rotate (create a new key and revoke the old one atomically) */
  @org.springframework.transaction.annotation.Transactional
  public NewKeyDto rotate(java.util.UUID oldKeyId, String churchId, String by) {
    revokeKey(oldKeyId, churchId);
    return createKey(churchId, by);
  }
}
