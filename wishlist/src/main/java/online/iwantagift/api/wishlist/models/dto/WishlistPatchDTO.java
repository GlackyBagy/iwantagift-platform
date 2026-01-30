package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for partially updating an existing wishlist.
 *
 * <p>This DTO is used in PATCH operations. All fields are optional.
 * Only non-null fields will be applied to the target wishlist.</p>
 *
 * <p>Fields omitted or set to {@code null} will not be modified.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistPatchDTO {
    /**
     * New display title of the wishlist.
     *
     * <p>If {@code null}, the title will not be changed.</p>
     */
    @JsonProperty("title")
    private String title;

    /**
     * New description of the wishlist.
     *
     * <p>If {@code null}, the description will not be changed.</p>
     */
    @JsonProperty("description")
    private String description;
}
