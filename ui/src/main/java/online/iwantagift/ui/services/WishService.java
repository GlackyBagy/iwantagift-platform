package online.iwantagift.ui.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.wl.WishDTO;
import online.iwantagift.ui.models.payloads.WishPayload;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishService {
    private RestClient restClients;
    private final IwagProperties iwagProperties;
    private final ClientHttpRequestFactory requestFactory;

    @PostConstruct
    public void init() {
        restClients = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(iwagProperties.getRequiredService("wishlists").getBaseUrl())
                .build();
    }

    public UUID createWish(WishPayload payload) {
        var response = restClients
                .post()
                .uri("/api/v1/wish")
                .body(payload)
                .retrieve()
                .onStatus(new HttpErrorHandler());


        return getCreatedEntityIdOrThrow(response);
    }

    static UUID getCreatedEntityIdOrThrow(RestClient.ResponseSpec response) {
        ParameterizedTypeReference<Map<String, UUID>> type = new ParameterizedTypeReference<>() {
        };

        Map<String, UUID> body = Optional.ofNullable(response.body(type))
                .orElseThrow(() -> new RemoteServiceException("Invalid response"));

        if (!body.containsKey("id"))
            throw new RemoteServiceException("Invalid response: id not found");
        return body.get("id");
    }

    public WishDTO findWishById(UUID wishId) {
        return restClients
                .get()
                .uri("/api/v1/wish/{id}", wishId)
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .toEntity(WishDTO.class)
                .getBody();
    }

    public void updateWish(WishPayload payload) {
         restClients.
                patch()
                .uri("/api/v1/wish")
                .body(payload)
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .toBodilessEntity();
    }
}
