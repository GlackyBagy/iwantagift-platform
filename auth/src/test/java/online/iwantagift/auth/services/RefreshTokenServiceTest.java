package online.iwantagift.auth.services;

import online.iwantagift.auth.models.entities.RefreshToken;
import online.iwantagift.auth.repositories.RefreshTokenRepository;
import online.iwantagift.auth.util.Sha256Encoder;
import online.iwantagift.auth.util.exceptions.TokenNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies {@link RefreshTokenService} contracts as documented in Javadoc.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository repository;

    @InjectMocks
    RefreshTokenService service;

    // ──────────────────────────────────────────────────────────────
    // findUserIdByRefreshToken
    // ──────────────────────────────────────────────────────────────

    @Test
    void findUserIdByRefreshToken_found_returnsUserId() {
        UUID userId = UUID.randomUUID();
        String rawToken = "raw-token";
        RefreshToken entity = activeToken(userId);
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.of(entity));

        UUID result = service.findUserIdByRefreshToken(rawToken);

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void findUserIdByRefreshToken_notFound_throwsTokenNotFoundException() {
        String rawToken = "missing-token";
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TokenNotFoundException.class)
                .isThrownBy(() -> service.findUserIdByRefreshToken(rawToken));
    }

    @Test
    void findUserIdByRefreshToken_doesNotCheckTokenState_returnsUserIdForRevokedToken() {
        // Contract: does not verify revoked/replaced/expired
        UUID userId = UUID.randomUUID();
        String rawToken = "revoked-token";
        RefreshToken revoked = revokedToken(userId);
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.of(revoked));

        assertThat(service.findUserIdByRefreshToken(rawToken)).isEqualTo(userId);
    }

    // ──────────────────────────────────────────────────────────────
    // isTokenValid
    // ──────────────────────────────────────────────────────────────

    @Test
    void isTokenValid_activeToken_returnsTrue() {
        String rawToken = "active";
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken)))
                .thenReturn(Optional.of(activeToken(UUID.randomUUID())));

        assertThat(service.isTokenValid(rawToken)).isTrue();
    }

    @Test
    void isTokenValid_revokedToken_returnsFalse() {
        String rawToken = "revoked";
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken)))
                .thenReturn(Optional.of(revokedToken(UUID.randomUUID())));

        assertThat(service.isTokenValid(rawToken)).isFalse();
    }

    @Test
    void isTokenValid_replacedToken_returnsFalse() {
        String rawToken = "replaced";
        RefreshToken replaced = activeToken(UUID.randomUUID());
        replaced.setReplacedBy(99L);
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.of(replaced));

        assertThat(service.isTokenValid(rawToken)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String rawToken = "expired";
        RefreshToken expired = activeToken(UUID.randomUUID());
        expired.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.of(expired));

        assertThat(service.isTokenValid(rawToken)).isFalse();
    }

    @Test
    void isTokenValid_missingToken_returnsFalse() {
        String rawToken = "nonexistent";
        when(repository.findByTokenHash(Sha256Encoder.encode(rawToken))).thenReturn(Optional.empty());

        assertThat(service.isTokenValid(rawToken)).isFalse();
    }

    // ──────────────────────────────────────────────────────────────
    // createRefreshTokenByUserId
    // ──────────────────────────────────────────────────────────────

    @Test
    void createRefreshTokenByUserId_returnsRawToken_andPersistsHash() {
        UUID userId = UUID.randomUUID();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(repository.save(any())).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        String rawToken = service.createRefreshTokenByUserId(userId);

        verify(repository).save(captor.capture());
        RefreshToken persisted = captor.getValue();

        assertThat(rawToken).isNotBlank();
        assertThat(persisted.getTokenHash()).isEqualTo(Sha256Encoder.encode(rawToken));
        assertThat(persisted.getUserId()).isEqualTo(userId);
        assertThat(persisted.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void createRefreshTokenByUserId_setExpirationToApprox3Days() {
        UUID userId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        service.createRefreshTokenByUserId(userId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        Instant expiresAt = captor.getValue().getExpiresAt();
        // Should be ~3 days from now (within 5 seconds tolerance)
        assertThat(expiresAt).isBetween(
                Instant.now().plus(3, ChronoUnit.DAYS).minusSeconds(5),
                Instant.now().plus(3, ChronoUnit.DAYS).plusSeconds(5)
        );
    }

    // ──────────────────────────────────────────────────────────────
    // rotate
    // ──────────────────────────────────────────────────────────────

    @Test
    void rotate_validToken_revokesOldAndReturnsNewRawToken() {
        UUID userId = UUID.randomUUID();
        String oldRaw = "old-raw";
        RefreshToken old = activeToken(userId);
        old.setId(1L);

        when(repository.findByTokenHashForUpdate(Sha256Encoder.encode(oldRaw))).thenReturn(Optional.of(old));
        when(repository.save(any())).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        String newRaw = service.rotate(oldRaw);

        assertThat(newRaw).isNotBlank().isNotEqualTo(oldRaw);
        assertThat(old.getRevokedAt()).isNotNull();
        assertThat(old.getReplacedBy()).isEqualTo(2L);
    }

    @Test
    void rotate_tokenNotFound_throwsTokenNotFoundException() {
        String raw = "missing";
        when(repository.findByTokenHashForUpdate(Sha256Encoder.encode(raw))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TokenNotFoundException.class)
                .isThrownBy(() -> service.rotate(raw));
    }

    @Test
    void rotate_revokedToken_throwsBadCredentialsException() {
        String raw = "revoked";
        RefreshToken revoked = revokedToken(UUID.randomUUID());
        when(repository.findByTokenHashForUpdate(Sha256Encoder.encode(raw))).thenReturn(Optional.of(revoked));

        assertThatExceptionOfType(BadCredentialsException.class)
                .isThrownBy(() -> service.rotate(raw));
    }

    @Test
    void rotate_replacedToken_throwsBadCredentialsException() {
        String raw = "replaced";
        RefreshToken replaced = activeToken(UUID.randomUUID());
        replaced.setReplacedBy(10L);
        when(repository.findByTokenHashForUpdate(Sha256Encoder.encode(raw))).thenReturn(Optional.of(replaced));

        assertThatExceptionOfType(BadCredentialsException.class)
                .isThrownBy(() -> service.rotate(raw));
    }

    @Test
    void rotate_expiredToken_throwsBadCredentialsException() {
        String raw = "expired";
        RefreshToken expired = activeToken(UUID.randomUUID());
        expired.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(repository.findByTokenHashForUpdate(Sha256Encoder.encode(raw))).thenReturn(Optional.of(expired));

        assertThatExceptionOfType(BadCredentialsException.class)
                .isThrownBy(() -> service.rotate(raw));
    }

    // ──────────────────────────────────────────────────────────────
    // revokeToken
    // ──────────────────────────────────────────────────────────────

    @Test
    void revokeToken_setsRevokedAtAndClearsReplacedBy() {
        String raw = "active";
        RefreshToken token = activeToken(UUID.randomUUID());
        token.setReplacedBy(5L);
        when(repository.findByTokenHash(Sha256Encoder.encode(raw))).thenReturn(Optional.of(token));

        service.revokeToken(raw);

        assertThat(token.getRevokedAt()).isNotNull();
        assertThat(token.getReplacedBy()).isNull();
    }

    @Test
    void revokeToken_alreadyRevoked_doesNotOverwriteRevokedAt() {
        String raw = "already-revoked";
        RefreshToken token = revokedToken(UUID.randomUUID());
        Instant originalRevokedAt = token.getRevokedAt();
        when(repository.findByTokenHash(Sha256Encoder.encode(raw))).thenReturn(Optional.of(token));

        service.revokeToken(raw);

        assertThat(token.getRevokedAt()).isEqualTo(originalRevokedAt);
    }

    @Test
    void revokeToken_notFound_throwsTokenNotFoundException() {
        String raw = "missing";
        when(repository.findByTokenHash(Sha256Encoder.encode(raw))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TokenNotFoundException.class)
                .isThrownBy(() -> service.revokeToken(raw));
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private RefreshToken activeToken(UUID userId) {
        RefreshToken t = new RefreshToken();
        t.setUserId(userId);
        t.setTokenHash("hash");
        t.setIssuedAt(Instant.now());
        t.setExpiresAt(Instant.now().plus(3, ChronoUnit.DAYS));
        return t;
    }

    private RefreshToken revokedToken(UUID userId) {
        RefreshToken t = activeToken(userId);
        t.setRevokedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        return t;
    }
}
