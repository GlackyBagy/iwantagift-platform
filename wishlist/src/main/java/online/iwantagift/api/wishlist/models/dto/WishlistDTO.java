package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing a wishlist in API requests and responses.
 *
 * <p>System-managed fields such as {@code id}, {@code ownerId}, {@code createdAt},
 * and {@code wishes} are returned by read operations but ignored or rejected for
 * write operations. The owner is resolved from the authenticated user.</p>
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
     * Managed by persistence and must not be provided by clients.
     */
    @JsonProperty("id")
    @Null(groups = ValidationGroups.Create.class)
    private UUID id;

    /**
     * Display title of the wishlist.
     */
    @JsonProperty("title")
    @Length(min = 1, max = 255)
    private String title;

    /**
     * Optional description of the wishlist.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Timestamp when the wishlist was created (UTC).
     * Managed by persistence and must not be provided by clients.
     */
    @JsonProperty("createdAt")
    @Null
    private Instant createdAt;

    /**
     * Unique identifier of the wishlist owner.
     * Resolved from authentication on writes.
     */
    @JsonProperty("owner_id")
    @Null
    private UUID ownerId;

    /**
     * List of wishes associated with this wishlist.
     * Returned by read operations and ignored on writes.
     */
    @JsonProperty("wishes")
    @Builder.Default
    private List<WishDTO> wishes = Collections.emptyList();
}
