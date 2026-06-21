package online.iwantagift.ui.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.payloads.WishlistPayload;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static online.iwantagift.ui.services.WishService.getCreatedEntityIdOrThrow;

@Service
@RequiredArgsConstructor
public class WishlistService {
    public static final String DEFAULT_WISHLIST_TITLE = "DEFAULT_WISHLIST";

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

    public UUID createWishlist(WishlistPayload payload, String accessToken) {
        var response = restClients
                .post()
                .uri("/api/v1/list")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(payload)
                .retrieve()
                .onStatus(new HttpErrorHandler());

        return getCreatedEntityIdOrThrow(response);
    }

    public void updateWishlist(WishlistPayload payload, String accessToken) {
        restClients
                .patch()
                .uri("/api/v1/list")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(payload)
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .toBodilessEntity();
    }

    public WishlistDTO getWishlist(UUID id) {
        WishlistDTO body = restClients
                .get()
                .uri("/api/v1/list/{id}", id)
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .body(WishlistDTO.class);

        return Optional.ofNullable(body)
                .orElseThrow(() -> new RemoteServiceException("Invalid response"));
    }

    public List<WishlistDTO> getAllWishlists(UUID userId) {
        var response = restClients
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/list/userLists")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .onStatus(new HttpErrorHandler());

        ParameterizedTypeReference<List<WishlistDTO>> type = new ParameterizedTypeReference<>() {
        };

        return Optional.ofNullable(response.body(type))
                .orElseThrow(() -> new RemoteServiceException("Invalid response"));
    }
}
