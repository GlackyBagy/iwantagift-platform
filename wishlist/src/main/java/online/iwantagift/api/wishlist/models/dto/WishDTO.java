package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object representing a wish item in API requests and responses.
 *
 * <p>System-managed fields such as {@code id}, {@code createdAt}, and
 * {@code ownerId} are returned by read operations but ignored or rejected for
 * write operations. The owner is resolved from the authenticated user.</p>
 *
 * <p>Timestamps are represented in UTC using {@link java.time.Instant}
 * to ensure consistent time handling across distributed services.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishDTO {

    /**
     * Unique identifier of the wish item.
     * Must be omitted when creating a wish.
     */
    @JsonProperty("id")
    @Null(groups = ValidationGroups.Create.class)
    private UUID id;

    /**
     * Display title of the wish item.
     */
    @JsonProperty("title")
    @NotBlank
    private String title;

    /**
     * Optional description of the wish item.
     */
    @JsonProperty("description")
    private String description;

    /**
     * URL of the product or external resource associated with the wish.
     * Must be a valid URL and must not be blank.
     * Maximum allowed length is 1000 characters.
     */
    @JsonProperty("url")
    @NotBlank
    @Length(max = 1000)
    @URL
    private String url;

    /**
     * Timestamp when the wish item was created (UTC).
     * Managed by persistence and must not be provided by clients.
     */
    @JsonProperty("createdAt")
    @Null
    private Instant createdAt;

    /**
     * Identifier of the wishlist to which this wish item belongs.
     * If omitted on create, the user's default wishlist is used.
     */
    @JsonProperty("wish_list_id")
    private UUID wishListId;

    /**
     * Ownership is resolved from the authenticated user and this value is ignored on writes.
     */
    @JsonProperty("owner_id")
    @Deprecated
    private UUID ownerId;
}
