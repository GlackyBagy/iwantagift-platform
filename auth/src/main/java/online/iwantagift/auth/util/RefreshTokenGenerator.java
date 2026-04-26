package online.iwantagift.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class RefreshTokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private RefreshTokenGenerator() {}

    public static String generateRefreshToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return B64.encodeToString(bytes);
    }
}