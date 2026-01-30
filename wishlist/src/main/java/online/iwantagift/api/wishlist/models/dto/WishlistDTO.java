package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing a wishlist returned by the API.
 *
 * <p>This DTO is used for read operations and contains system-managed fields
 * such as {@code id}, {@code ownerId}, and {@code createdAt}.</p>
 *
 * <p>The {@code wishes} field contains the list of wish items that belong
 * to this wishlist. The list may be empty but is never {@code null}.</p>
 *
 * <p>Timestamps are represented in UTC. The {@code createdAt} field is
 * provided as an {@link java.time.Instant} to represent an absolute moment
 * in time.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    /**
     * Unique identifier of the wishlist.
     */
    @JsonProperty("id")
    private UUID id;

    /**
     * Display title of the wishlist.
     */
    @JsonProperty("title")
    private String title;

    /**
     * Optional description of the wishlist.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Timestamp when the wishlist was created (UTC).
     */
    @JsonProperty("createdAt")
    private Instant createdAt;

    /**
     * Unique identifier of the wishlist owner.
     */
    @JsonProperty("owner_id")
    private UUID ownerId;

    /**
     * List of wishes associated with this wishlist.
     */
    @JsonProperty("wishes")
    private List<WishDTO> wishes = Collections.emptyList();
}
