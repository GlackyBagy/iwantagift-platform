package online.iwantagift.ui.security.services;

import io.jsonwebtoken.security.JwkSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthService {

    @Value("${iwag.services.auth.url}")
    String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public JwkSet getJwkSet() throws RuntimeException {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(authServiceUrl)
                .path("/.well-known/jwks.json")
                .build();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<JwkSet> response = restTemplate.getForEntity(uriComponents.toUri(), JwkSet.class);

        if (!response.getStatusCode().is2xxSuccessful())
            throw new RuntimeException("Bad response: %s".formatted(response.toString()));

        return response.getBody();
    }
}
