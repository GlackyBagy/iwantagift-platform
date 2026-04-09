package online.iwantagift.ui.security.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.PublicJwk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final AuthService authService;

    private PublicKey publicKey;

    @PostConstruct
    protected void init() throws URISyntaxException {
        JwkSet set = authService.getJwkSet(); //todo handle exception

        PublicKey publicKey = set.getKeys().stream()
                .filter(jwk -> "jwt-sign".equals(jwk.getId()))
                .filter(jwk -> jwk instanceof PublicJwk<?>)
                .map(jwk -> (PublicJwk<?>) jwk)
                .map(pj -> (PublicKey) pj.toKey())
                .findFirst()
                .orElse(null);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        Date expirationDate = claims.getExpiration();

        return username.equals(userDetails.getUsername())
                && expirationDate.after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}