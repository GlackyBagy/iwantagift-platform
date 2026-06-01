package online.iwantagift.auth.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenGeneratorTest {

    @Test
    void generateRefreshToken_notNullOrEmpty() {
        assertThat(RefreshTokenGenerator.generateRefreshToken()).isNotBlank();
    }

    @Test
    void generateRefreshToken_isUrlSafe() {
        String token = RefreshTokenGenerator.generateRefreshToken();
        // URL-safe Base64 uses '-' and '_' instead of '+' and '/', no padding '='
        assertThat(token).doesNotContain("+", "/", "=");
    }

    @Test
    void generateRefreshToken_producesUniqueValues() {
        String first = RefreshTokenGenerator.generateRefreshToken();
        String second = RefreshTokenGenerator.generateRefreshToken();
        assertThat(first).isNotEqualTo(second);
    }
}
