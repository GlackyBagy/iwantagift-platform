package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.entities.RefreshToken;
import online.iwantagift.auth.repositories.RefreshTokenRepository;
import online.iwantagift.auth.util.RefreshTokenGenerator;
import online.iwantagift.auth.util.Sha256Encoder;
import online.iwantagift.auth.util.exceptions.TokenNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages refresh token lifecycle operations.
 *
 * <p>This service is responsible for issuing, validating, rotating, and revoking refresh tokens.
 * Tokens are stored in hashed form (SHA-256 hex). Rotation uses pessimistic row locking to reduce
 * replay races for the same token.
 *
 * <p>Current limitations:
 * <ul>
 *   <li>{@link #findUserIdByRefreshToken(String)} checks token existence only and does not enforce
 *   revoked/replaced/expired constraints.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    /**
     * Resolves user ID by raw refresh token.
     *
     * <p>Contract:
     * <ul>
     *   <li>Input is a raw (not hashed) refresh token string.</li>
     *   <li>Method returns owner user ID if a token row exists.</li>
     *   <li>Method does not verify token state (revoked/replaced/expired).</li>
     * </ul>
     *
     * @param refreshToken raw refresh token.
     * @return owner user ID for the token.
     * @throws TokenNotFoundException if no token row exists for the provided value.
     */
    public UUID findUserIdByRefreshToken(String refreshToken) throws TokenNotFoundException {
        RefreshToken token = getTokenByRawValue(refreshToken);
        return token.getUserId();
    }

    /**
     * Checks whether a refresh token is currently active.
     *
     * <p>Contract:
     * <ul>
     *   <li>Returns {@code true} only when token exists and is not revoked, not replaced, and not expired.</li>
     *   <li>Returns {@code false} for missing tokens.</li>
     * </ul>
     *
     * <p>Unhandled cases:
     * <ul>
     *   <li>No row-level lock is used; result can become stale immediately under concurrent updates.</li>
     * </ul>
     *
     * @param refreshToken raw refresh token.
     * @return {@code true} if token is active; otherwise {@code false}.
     */
    public boolean isTokenValid(String refreshToken) {
        Optional<RefreshToken> refresh = repository
                .findByTokenHash(Sha256Encoder.encode(refreshToken));

        return refresh.map(x ->
                x.getRevokedAt() == null &&
                x.getReplacedBy() == null &&
                Instant.now().isBefore(x.getExpiresAt())
        ).orElse(false);
    }

    /**
     * Issues a new refresh token for a user and persists token metadata.
     *
     * <p>Contract:
     * <ul>
     *   <li>Generated token is URL-safe and returned in raw form.</li>
     *   <li>Stored value is SHA-256 hash of the raw token.</li>
     *   <li>Expiration is set to 3 days from creation time.</li>
     * </ul>
     *
     * <p>Errors:
     * <ul>
     *   <li>Propagates repository/data-access exceptions.</li>
     * </ul>
     *
     * @param userId token owner user ID.
     * @return newly issued raw refresh token.
     */
    public String createRefreshTokenByUserId(UUID userId) {
        String newRefreshToken = RefreshTokenGenerator.generateRefreshToken();
        createReplacementToken(userId, newRefreshToken);

        return newRefreshToken;
    }

    /**
     * Rotates a refresh token atomically.
     *
     * <p>Contract:
     * <ul>
     *   <li>Locates the old token row using pessimistic write lock.</li>
     *   <li>Rejects token if it is missing, revoked, already replaced, or expired.</li>
     *   <li>Creates a replacement token, revokes old token, links old token to replacement.</li>
     *   <li>Returns replacement token in raw form.</li>
     * </ul>
     *
     * <p>Errors:
     * <ul>
     *   <li>{@link TokenNotFoundException} when cannot find provided token in DB</li>
     *   <li>{@link BadCredentialsException} when token is invalid by any business rule above.</li>
     *   <li>Propagates repository/data-access exceptions.</li>
     * </ul>
     *
     * @param oldRawToken raw token to rotate.
     * @return newly issued raw refresh token.
     * @throws BadCredentialsException if old token is invalid.
     * @throws TokenNotFoundException if old token not found
     */
    @Transactional
    public String rotate(String oldRawToken) throws BadCredentialsException, TokenNotFoundException {
        RefreshToken old = repository.findByTokenHashForUpdate(Sha256Encoder.encode(oldRawToken))
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

        if (old.getRevokedAt() != null || old.getReplacedBy() != null || Instant.now().isAfter(old.getExpiresAt())) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String newRaw = RefreshTokenGenerator.generateRefreshToken();
        RefreshToken fresh = createReplacementToken(old.getUserId(), newRaw);

        old.setRevokedAt(Instant.now());
        old.setReplacedBy(fresh.getId());

        return newRaw;
    }

    /**
     * Revokes a token without replacement.
     *
     * <p>Contract:
     * <ul>
     *   <li>If token is not yet revoked, sets {@code revokedAt} to current instant.</li>
     *   <li>Always sets {@code replacedBy} to {@code null}.</li>
     * </ul>
     *
     * <p>Errors:
     * <ul>
     *   <li>{@link TokenNotFoundException} if token does not exist.</li>
     * </ul>
     *
     * @param refreshToken raw refresh token to revoke.
     * @throws TokenNotFoundException if token does not exist.
     */
    @Transactional
    public void revokeToken(String refreshToken) throws TokenNotFoundException {
        RefreshToken token = getTokenByRawValue(refreshToken);
        revokeToken(token, null);
    }

    @Transactional
    protected void revokeToken(RefreshToken token, Long replacedBy) {
        if (token.getRevokedAt() == null)
            token.setRevokedAt(Instant.now());
        token.setReplacedBy(replacedBy);
    }

    /**
     * Resolves persisted token entity by raw token value.
     *
     * @param refreshToken raw refresh token.
     * @return matching persisted token entity.
     * @throws TokenNotFoundException if token does not exist.
     */
    private RefreshToken getTokenByRawValue(String refreshToken) throws TokenNotFoundException {
        String tokenHash = Sha256Encoder.encode(refreshToken);
        return repository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));
    }

    /**
     * Creates and persists a replacement token entity.
     *
     * <p>Contract:
     * <ul>
     *   <li>Creates a new row with issued/expiry timestamps and hashed token value.</li>
     *   <li>Does not mutate old token state; caller is responsible for linking/revoking old token.</li>
     * </ul>
     *
     * @param userId owner user ID.
     * @param newRefreshToken raw token value to hash and store.
     * @return persisted replacement token entity.
     */
    private RefreshToken createReplacementToken(UUID userId, String newRefreshToken) {
        RefreshToken newToken = new RefreshToken();
        newToken.setUserId(userId);
        newToken.setIssuedAt(Instant.now());
        newToken.setExpiresAt(Instant.now().plus(3, ChronoUnit.DAYS));
        newToken.setTokenHash(Sha256Encoder.encode(newRefreshToken));

        return repository.save(newToken);
    }
}
