package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.dto.EmailChangeVerification;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final String KEY_PREFIX = "verification-token:";
    private static final String OLD_EMAIL_FIELD = "oldEmail";
    private static final String NEW_EMAIL_FIELD = "newEmail";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    public String generateAndSaveForEmail(String oldEmail, String newEmail) {
        String token = generateToken();
        String redisKey = redisKey(token);

        redisTemplate.opsForHash().putAll(redisKey, Map.of(
                OLD_EMAIL_FIELD, oldEmail,
                NEW_EMAIL_FIELD, newEmail
        ));
        redisTemplate.expire(redisKey, TOKEN_TTL);
        return token;
    }

    private static String generateToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public Optional<EmailChangeVerification> findEmailChange(String token) {
        Map<Object, Object> fields = find(token);
        Object oldEmail = fields.get(OLD_EMAIL_FIELD);
        Object newEmail = fields.get(NEW_EMAIL_FIELD);

        if (!(oldEmail instanceof String oldEmailValue)
                || !(newEmail instanceof String newEmailValue)) {
            return Optional.empty();
        }

        return Optional.of(new EmailChangeVerification(oldEmailValue, newEmailValue));
    }

    private Map<Object, Object> find(String token) {
        if (token == null || token.isBlank()) {
            return Map.of();
        }

        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(redisKey(token));
    }

    public boolean revoke(String token) {
        return Boolean.TRUE.equals(redisTemplate.delete(redisKey(token)));
    }

    private String redisKey(String token) {
        return KEY_PREFIX + token;
    }
}
