package online.iwantagift.api.wishlist.models.dto.abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishPatchDTO;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Base Data Transfer Object for write operations on wish items.
 *
 * <p>This abstract DTO defines the common set of writable fields
 * shared between wish creation and partial update (PATCH) operations.</p>
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@link WishCreateDTO} — used for creating a new wish item (POST).</li>
 *   <li>{@link WishPatchDTO} — used for partially updating an existing wish item (PATCH).</li>
 * </ul>
 *
 * <h3>PATCH semantics</h3>
 * <p>
 * For PATCH operations, all fields are treated as optional.
 * Only fields with non-{@code null} values will be applied to the target entity.
 * Fields omitted from the request or explicitly set to {@code null}
 * will not modify the existing values.
 * </p>
 *
 * <h3>Validation</h3>
 * <p>
 * Validation rules are applied using Bean Validation groups:
 * </p>
 * <ul>
 *   <li>{@code ValidationGroups.Create} — enforces required fields for creation.</li>
 *   <li>{@code ValidationGroups.Patch} — applies validation for partial updates.</li>
 * </ul>
 *
 * <p>
 * Constraints without explicit validation groups belong to the default group
 * and are applied to both create and patch operations unless explicitly overridden.
 * </p>
 *
 * <p>
 * This class does not include system-managed fields such as identifiers
 * or timestamps. Such fields must be handled by the service or persistence layer.
 * </p>
 *
 * @see WishCreateDTO
 * @see WishPatchDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class WishWriteDTO {
    /**
     * Display title of the wish item.
     * Must not be blank.
     */
    @JsonProperty("title")
    @NotBlank(groups = ValidationGroups.Create.class)
    @Length(min = 1)
    protected String title;

    /**
     * Optional description of the wish item.
     */
    @JsonProperty("description")
    protected String description;

    /**
     * URL of the product or external resource associated with the wish.
     * Must be a valid URL and must not be blank on create/put.
     * Maximum allowed length is 1000 characters.
     */
    @JsonProperty("url")
    @Length(max = 1000, message = "URL is too long")
    @URL
    @NotBlank(groups = ValidationGroups.Create.class)
    protected String url;

    /**
     * Identifier of the wishlist where the wish will be created.
     *
     * <p>If {@code null}, the wish will be automatically assigned
     * to the user's default wishlist.</p>
     */
    @JsonProperty("wish_list_id")
    protected UUID wishListId;

    /**
     * Identifier of the user who is creating the wish.
     *
     * <p>This field is required and is used to determine ownership
     * and access control for the created wish item.</p>
     */
    @JsonProperty("owner_id")
    @NotNull(groups = ValidationGroups.Create.class)
    protected UUID ownerId;
}
