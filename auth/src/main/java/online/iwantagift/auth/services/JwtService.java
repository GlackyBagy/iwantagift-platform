package online.iwantagift.auth.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.entities.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * Generates and validates JWT access tokens for authenticated users.
 *
 * <p>The service creates an in-memory RSA key pair during application startup and uses it to
 * sign and verify tokens. Generated tokens include the username as the subject and the user's
 * authorities in the {@code roles} claim.
 */
@Service
public class JwtService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AccountService accountService;
    @Value("${jwt.expiration}")
    private long expiration;

    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    public JwtService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.accountService = accountService;
    }

    /**
     * Generates the RSA key pair used to sign and verify JWT tokens.
     *
     * <p>The current implementation keeps the keys only in memory for the lifetime of the
     * application instance.
     */
    @PostConstruct
    void init() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // todo load from .pem files
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA keys", e);
        }
    }

    /**
     * Creates a signed JWT token for the given user
     *
     * @param userDetails authenticated user details used to populate token subject and roles
     * @return compact serialized JWT token
     */
    public String generateToken(UserDetails userDetails, UUID userId) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("userId", userId)
                .claim("nickname", Optional.ofNullable(accountService.findById(userId))
                        .flatMap(account -> account)
                        .map(Account::getNickname)
                        .orElse(userDetails.getUsername()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    /**
     * Creates a signed JWT token for the given user, looks for userId in database by provided email
     *
     * @param userDetails authenticated user details used to populate token subject and roles
     * @return compact serialized JWT token
     */
    public String generateToken(UserDetails userDetails) {
        UUID userId = accountService.userIdByEmail(userDetails.getUsername()).get();
        return generateToken(userDetails, userId);
    }

    /**
     * Extracts the username stored in the token subject.
     *
     * @param token signed JWT token
     * @return username from the token subject
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Checks whether the token belongs to the given user and is not expired.
     *
     * @param token signed JWT token
     * @param userDetails user details expected to match the token subject
     * @return {@code true} if the token subject matches the user and the token is still valid
     */
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

    public String jwtFromCredentials(CredentialsDTO credentials) {
        return jwtFromCredentials(credentials.getEmail(), credentials.getPassword());
    }

    public String jwtFromCredentials(String email,  String password) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email,
                        password
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return generateToken(Objects.requireNonNull(userDetails));
    }

    public String jwtFromUsername(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return generateToken(userDetails);
    }
}
