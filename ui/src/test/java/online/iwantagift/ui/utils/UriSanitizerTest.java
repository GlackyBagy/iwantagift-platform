package online.iwantagift.ui.utils;

import online.iwantagift.ui.util.UriSanitizer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UriSanitizerTest {
    @Mock
    private Logger loggerMock;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/me", "/profile?id=1344535#description",
            "/profile?id=1344535", "/donate/wish?id=13445&uid=4458vhg9564"})
    void sanitizeRedirect_rebuildsOnValidInput(String redirectUri) {
        String sanitized =  UriSanitizer.sanitizeRedirect(redirectUri, loggerMock);
        assertEquals(redirectUri, sanitized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"//", "example.com/g", "example.com",
            "https://example.com/g?f=435", "somePage#desc", "page"})
    void sanitizeRedirect_returnsRootOnInvalidInput(String redirectUri) {
        String sanitized =  UriSanitizer.sanitizeRedirect(redirectUri, loggerMock);
        assertEquals("/", sanitized);
    }
}
