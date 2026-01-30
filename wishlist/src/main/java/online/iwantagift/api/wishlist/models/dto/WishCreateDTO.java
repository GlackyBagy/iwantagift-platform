package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Data Transfer Object for creating a new wish item.
 *
 * <p>This DTO is used for write operations (POST) and should not contain
 * system-managed fields such as {@code id} or {@code createdAt}.</p>
 *
 * <p>
 * The {@code wishListId} field specifies the target wishlist where
 * the wish item will be created. If {@code wishListId} is {@code null},
 * the wish item will be automatically assigned to the user's default wishlist.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishCreateDTO {
    /**
     * Display title of the wish item.
     * Must not be blank.
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
    @Length(max = 1000, message = "URL is too long")
    @URL
    private String url;

    /**
     * Identifier of the wishlist where the wish will be created.
     *
     * <p>If {@code null}, the wish will be automatically assigned
     * to the user's default wishlist.</p>
     */
    @JsonProperty("wish_list_id")
    private UUID wishListId;

    /**
     * Identifier of the user who is creating the wish.
     *
     * <p>This field is required and is used to determine ownership
     * and access control for the created wish item.</p>
     */
    @JsonProperty("user_id")
    @NotNull
    private UUID userId;
}