package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for patching an existing wish item.
 *
 * <p>This DTO is used in PATCH operations.
 *  All fields are optional.
 * Only non-null fields will be applied to the target wish object.</p>
 *
 * <p>Fields omitted or set to {@code null} will not be modified.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishPatchDTO {

    /**
     * New display title of the wish item.
     *
     * <p>If {@code null}, the title will not be changed.</p>
     */
    @JsonProperty("title")
    private String title;

    /**
     * New description of the wish item.
     *
     * <p>If {@code null}, the description will not be changed.</p>
     */
    @JsonProperty("description")
    private String description;

    /**
     * New URL of the product or external resource associated with the wish.
     *
     * <p>If {@code null}, the URL will not be changed.</p>
     *
     * <p>Must be a valid URL. Maximum allowed length is 1000 characters.</p>
     */
    @JsonProperty("url")
    @Length(max = 1000, message = "URL is too long")
    @URL
    private String url;
}