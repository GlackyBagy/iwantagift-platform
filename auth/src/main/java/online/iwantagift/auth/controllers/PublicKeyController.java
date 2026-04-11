package online.iwantagift.auth.controllers;

import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.PublicJwk;
import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.services.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

/**
 * Publishes the JSON Web Key Set used by clients to verify JWT signatures.
 */
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class PublicKeyController {

    private final JwtService jwtService;

    /**
     * Returns the public signing key as a JWKS document.
     *
     * @return JSON Web Key Set containing the RSA public key used for JWT verification
     */
    @GetMapping(value = "/jwks.json")
    public Object jwksJson() {
        PublicJwk<PublicKey> signKey = Jwks.builder()
                .key(jwtService.getPublicKey())
                .id("jwt-sign")
                .algorithm("RS256")
                .build();

        return Jwks.set()
                .add(signKey)
                .build();
    }
}
