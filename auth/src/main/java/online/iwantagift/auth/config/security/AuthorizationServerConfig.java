package online.iwantagift.auth.config.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.services.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@Slf4j
public class AuthorizationServerConfig {

    /**
     * Filesystem location of the PKCS#8 PEM private key used to sign tokens. If absent, a key is
     * generated and written here on first start so it stays stable across restarts. Point this at a
     * mounted secret in production.
     */
    @Value("${iwag.auth.jwt.key-path:data/auth-jwt-private.pem}")
    private String jwtKeyPath;


    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                // Browser requests (e.g. /oauth2/authorize) are redirected to the login page,
                // while API requests with a Bearer token (e.g. /userinfo) get a proper 401.
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Required for /userinfo: it is called by the ui with a Bearer access token.
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    /**
     * Decoder for validating this server's own access tokens (signed with {@link #jwkSource()}).
     * Exposed as a bean so resource-server security chains (e.g. the Bearer-protected {@code /auth}
     * REST endpoints) can authenticate access tokens, not just the authorization-server chain.
     */
    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = loadOrCreateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        try {
            // Thumbprint-based key id is derived from the key itself, so it is stable across
            // restarts as long as the key is — old tokens keep matching the published JWK.
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyIDFromThumbprint()
                    .build();

            JWKSet jwkSet = new JWKSet(rsaKey);
            return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
        } catch (JOSEException ex) {
            throw new IllegalStateException("Failed to build RSA JWK", ex);
        }
    }

    /**
     * Loads the RSA key pair from {@link #jwtKeyPath}, or generates and persists one there on first
     * run. Persisting the key keeps issued JWT access tokens valid across auth restarts (the signing
     * key — and therefore the published JWK — no longer changes on every boot).
     */
    private KeyPair loadOrCreateKeyPair() {
        Path keyFile = Path.of(jwtKeyPath);
        try {
            if (Files.exists(keyFile)) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(pemToDer(Files.readString(keyFile))));
                // The public key is reconstructed from the private CRT key (it carries the modulus
                // and public exponent), so only the private key needs to be stored.
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                        new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
                log.info("Loaded RSA signing key from {}", keyFile.toAbsolutePath());
                return new KeyPair(publicKey, privateKey);
            }

            KeyPair generated = generateRsaKey();
            Path parent = keyFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(keyFile, derToPem(generated.getPrivate().getEncoded()));
            log.warn("No RSA signing key at {} — generated and persisted a new one. "
                            + "Provide a stable key (iwag.auth.jwt.key-path) for production.",
                    keyFile.toAbsolutePath());
            return generated;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load or create RSA signing key", ex);
        }
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    private static byte[] pemToDer(String pem) {
        String base64 = pem.replaceAll("-----BEGIN [^-]*-----", "")
                .replaceAll("-----END [^-]*-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private static String derToPem(byte[] der) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    /**
     * Persists issued authorizations (authorization codes, access and refresh tokens) in the
     * database instead of the default in-memory store. Without this, an auth restart wipes every
     * issued refresh token, so the next refresh attempt fails with {@code invalid_grant}.
     */
    @Bean
    OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                    RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder, IwagProperties iwagProperties) throws URISyntaxException {
        IwagProperties.ServiceProperties uiProps = iwagProperties.requireService("ui");
        String redirectUri = UriComponentsBuilder.fromUri(uiProps.requireBaseUrl().toURI())
                .path("/login/oauth2/code/iwag-ui")
                .toUriString();

        RegisteredClient uiClient = RegisteredClient.withId("iwag-ui")
                .clientId("iwag-ui")
                .clientSecret(passwordEncoder.encode(uiProps.requireClientSecret()))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .scope("openid")
                .scope("profile")
                .build();

        return new InMemoryRegisteredClientRepository(uiClient);
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer(AccountService accountService) {
        return context -> {
            boolean supportedToken = OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
                    || OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue());
            if (!supportedToken) {
                return;
            }

            String email = context.getPrincipal().getName();
            accountService.userIdByEmail(email)
                    .ifPresent(userId -> context.getClaims().claim("userId", userId.toString()));
        };
    }
}
