package online.iwantagift.ui.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UriSanitizer {

    /**
     * Normalizes a redirect target so that only local application paths are allowed.
     *
     * <p>Absolute URIs, host-qualified values, malformed values, and blank paths are replaced with
     * the application root path.
     *
     * @param redirect the requested redirect target
     * @return a safe local redirect target beginning with {@code /}
     */
    public static String sanitizeRedirect(String redirect, Logger log) {
        if (redirect == null || redirect.isBlank()) {
            log.info("Empty redirect target, using '/'");
            return "/";
        }

        try {
            URI uri = new URI(redirect);

            if (uri.isAbsolute() || uri.getHost() != null) {
                log.debug("Rejected non-local redirect target '{}', using '/'", redirect);
                return "/";
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            String fragment = uri.getRawFragment();

            if (path == null || path.isBlank() || !path.startsWith("/")) {
                log.debug("Rejected malformed redirect path '{}', using '/'", redirect);
                return "/";
            }

            StringBuilder safe = new StringBuilder(path);

            if (query != null && !query.isBlank())
                safe.append('?').append(query);

            if (fragment != null && !fragment.isBlank())
                safe.append('#').append(fragment);

            log.info("Sanitized redirect '{}' to '{}'", redirect, safe);
            return safe.toString();
        } catch (URISyntaxException e) {
            log.debug("Rejected invalid redirect URI '{}', using '/'", redirect);
            return "/";
        }
    }
}
