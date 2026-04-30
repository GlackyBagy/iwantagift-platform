package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.services.JwtService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthRefreshController {

    private final JwtCookieFactory jwtCookieFactory;
    private final AuthService authClient;
    private final JwtService jwtService;

    @GetMapping("/auth/refresh") //todo use
    public String refresh(@RequestParam(name = "redirect", required = false) String redirect,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        Optional<String> refreshToken = jwtService.extractRefreshToken(request);

        if (refreshToken.isEmpty() || refreshToken.get().isBlank()) {
            clearAuthCookies(response);
            return "redirect:/signin";
        }

        try {
            TokenDTO tokens = authClient.refresh(refreshToken.get());

            jwtCookieFactory.createAuthCookies(tokens.getJwtToken(), tokens.getRefreshToken())
                    .forEach(response::addCookie);

            return "redirect:" + sanitizeRedirect(redirect);
        } catch (Exception ex) {
            clearAuthCookies(response);
            return "redirect:/signin";
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        jwtCookieFactory.createLogoutCookies()
                .forEach(response::addCookie);
    }

    private String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/";
        }

        try {
            URI uri = new URI(redirect);

            if (uri.isAbsolute() || uri.getHost() != null)
                return "/";

            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            String fragment = uri.getRawFragment();

            if (path == null || path.isBlank() || !path.startsWith("/"))
                return "/";

            StringBuilder safe = new StringBuilder(path);

            if (query != null && !query.isBlank())
                safe.append('?').append(query);

            if (fragment != null && !fragment.isBlank())
                safe.append('#').append(fragment);

            return safe.toString();
        } catch (URISyntaxException e) {
            return "/";
        }
    }
}