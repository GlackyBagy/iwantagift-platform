package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.UUID;
/**
 * Data Transfer Object representing a wish item returned by the API.
 *
 * <p>This DTO is used for read operations and contains system-managed fields
 * such as {@code id} and {@code createdAt}.</p>
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
     */
    @JsonProperty("id")
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
     */
    @JsonProperty("createdAt")
    private Instant createdAt;

    /**
     * Identifier of the wishlist to which this wish item belongs.
     */
    @JsonProperty("wish_list_id")
    private UUID wishListId;

    @JsonProperty("owner_id")
    private UUID ownerId;
}