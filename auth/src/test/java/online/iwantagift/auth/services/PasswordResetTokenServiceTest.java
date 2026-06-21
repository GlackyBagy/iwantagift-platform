package online.iwantagift.auth.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    private static final Duration TTL = Duration.ofMinutes(15);

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;

    @Test
    void create_reservesEmailAndStoresToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq("password-reset-pending:user@example.com"), anyString(), eq(TTL)))
                .thenReturn(true);
        PasswordResetTokenService service = new PasswordResetTokenService(redisTemplate);

        Optional<String> token = service.create("user@example.com");

        assertTrue(token.isPresent());
        verify(valueOperations).set(
                "password-reset-token:" + token.get(), "user@example.com", TTL);
    }

    @Test
    void create_returnsEmptyWhenResetAlreadyPending() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq("password-reset-pending:user@example.com"), anyString(), eq(TTL)))
                .thenReturn(false);
        PasswordResetTokenService service = new PasswordResetTokenService(redisTemplate);

        Optional<String> token = service.create("user@example.com");

        assertFalse(token.isPresent());
        verify(valueOperations, never()).set(anyString(), anyString(), eq(TTL));
    }

    @Test
    void consume_resolvesEmailAndDeletesBothKeys() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("password-reset-token:token")).thenReturn("user@example.com");
        PasswordResetTokenService service = new PasswordResetTokenService(redisTemplate);

        Optional<String> email = service.consume("token");

        assertEquals(Optional.of("user@example.com"), email);
        verify(redisTemplate).delete("password-reset-token:token");
        verify(redisTemplate).delete("password-reset-pending:user@example.com");
    }

    @Test
    void consume_returnsEmptyForUnknownToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("password-reset-token:missing")).thenReturn(null);
        PasswordResetTokenService service = new PasswordResetTokenService(redisTemplate);

        assertFalse(service.consume("missing").isPresent());
        verify(redisTemplate, never()).delete(anyString());
    }
}
