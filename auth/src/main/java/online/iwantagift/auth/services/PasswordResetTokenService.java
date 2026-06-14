package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * Stores single-use password-reset tokens in Redis.
 *
 * <p>Two keys are kept per reset, both with the same TTL:
 * <ul>
 *     <li>{@code password-reset-token:{token}} → email, used to resolve the token on confirmation;</li>
 *     <li>{@code password-reset-pending:{email}} → token, used to reject a second reset request while
 *     the first is still alive.</li>
 * </ul>
 *
 * <p>The pending key is created with {@code SET ... NX} so that concurrent requests for the same
 * email cannot both succeed.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final String TOKEN_KEY_PREFIX = "password-reset-token:";
    private static final String PENDING_KEY_PREFIX = "password-reset-pending:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    /**
     * Reserves a reset for {@code email} and returns the new token.
     *
     * @return the generated token, or {@link Optional#empty()} if a reset is already pending for the
     * given email.
     */
    public Optional<String> create(String email) {
        String token = generateToken();

        Boolean reserved = redisTemplate.opsForValue()
                .setIfAbsent(pendingKey(email), token, TOKEN_TTL);
        if (!Boolean.TRUE.equals(reserved)) {
            return Optional.empty();
        }

        redisTemplate.opsForValue().set(tokenKey(token), email, TOKEN_TTL);
        return Optional.of(token);
    }

    /**
     * Consumes the token: resolves the associated email and removes both keys so the token cannot be
     * reused.
     */
    public Optional<String> consume(String token) {
        return remove(token);
    }

    /** Cancels a pending reset identified by {@code token}. */
    public void cancel(String token) {
        remove(token);
    }

    private Optional<String> remove(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String email = redisTemplate.opsForValue().get(tokenKey(token));
        if (email == null) {
            return Optional.empty();
        }

        redisTemplate.delete(tokenKey(token));
        redisTemplate.delete(pendingKey(email));
        return Optional.of(email);
    }

    private static String generateToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    private String pendingKey(String email) {
        return PENDING_KEY_PREFIX + email;
    }
}
