package online.iwantagift.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailVerificationUrlBuilderTest {

    @Test
    void build_ForEmail_createsEncodedAuthVerificationUrl() {
        EmailVerificationUrlBuilder builder =
                new EmailVerificationUrlBuilder("https://auth.example.com/");

        String url = builder.buildForEmail("a/b");

        assertEquals(
                "https://auth.example.com/auth/verify/email?token=a%2Fb",
                url
        );
    }
}
