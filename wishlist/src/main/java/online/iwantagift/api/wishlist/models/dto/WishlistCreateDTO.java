package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new wishlist.
 *
 * <p>This DTO is used in POST operations to create a wishlist.
 * System-managed fields such as {@code id}, {@code ownerId}, and
 * {@code createdAt} are not allowed and will be ignored if provided.</p>
 *
 * <p>The {@code title} field is required and represents the display
 * name of the wishlist.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCreateDTO {
    /**
     * Display title of the wishlist.
     * Must not be blank.
     */
    @JsonProperty("title")
    @NotBlank
    private String title;

    /**
     * Optional description of the wishlist.
     */
    @JsonProperty("description")
    private String description;
}