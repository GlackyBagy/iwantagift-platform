package online.iwantagift.auth.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates random plaintext passwords for the password-reset flow.
 *
 * <p>The character set excludes visually ambiguous characters (0/O, 1/l/I) to reduce transcription
 * errors when the user copies the password from the reset email.
 */
@Component
public class PasswordGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
    private static final int LENGTH = 16;

    public String generate() {
        StringBuilder password = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            password.append(ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)]);
        }
        return password.toString();
    }
}
