package online.iwantagift.auth.services;

import online.iwantagift.auth.models.dto.VerificationViaEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock HashOperations<String, Object, Object> hashOperations;

    @Test
    void generateAndSave_storesEmailChangeAndReturnsTokenForEmail() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        VerificationTokenService service = new VerificationTokenService(redisTemplate);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        String token = service.generateAndSaveForEmail("old@example.com", "new@example.com");

        verify(hashOperations).putAll(
                keyCaptor.capture(),
                eq(Map.of(
                        "oldEmail", "old@example.com",
                        "newEmail", "new@example.com"
                ))
        );
        assertEquals("verification-token:" + token, keyCaptor.getValue());
        verify(redisTemplate).expire(
                "verification-token:" + token,
                Duration.ofMinutes(15)
        );
    }

    @Test
    void findEmailChange_returnsStoredEmailChange() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("verification-token:token")).thenReturn(Map.of(
                "oldEmail", "old@example.com",
                "newEmail", "new@example.com"
        ));
        VerificationTokenService service = new VerificationTokenService(redisTemplate);

        Optional<VerificationViaEmail> result = service.findEmailChange("token");

        assertEquals(
                new VerificationViaEmail("old@example.com", "new@example.com"),
                result.orElseThrow()
        );
    }

    @Test
    void findEmailChange_returnsEmptyForMissingToken() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("verification-token:missing")).thenReturn(Map.of());
        VerificationTokenService service = new VerificationTokenService(redisTemplate);

        assertFalse(service.findEmailChange("missing").isPresent());
    }

    @Test
    void findEmailChange_rejectsNonEmailToken() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("verification-token:token")).thenReturn(Map.of(
                "email", "user@example.com",
                "passwordHash", "encoded-password"
        ));
        VerificationTokenService service = new VerificationTokenService(redisTemplate);

        assertTrue(service.findEmailChange("token").isEmpty());
    }

    @Test
    void revoke_deletesStoredToken() {
        when(redisTemplate.delete("verification-token:token")).thenReturn(true);
        VerificationTokenService service = new VerificationTokenService(redisTemplate);

        assertTrue(service.revoke("token"));
    }
}
