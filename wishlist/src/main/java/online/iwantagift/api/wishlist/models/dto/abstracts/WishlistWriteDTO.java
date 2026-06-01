package online.iwantagift.api.wishlist.models.dto.abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistCreateDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPatchDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPutDTO;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

/**
 * Base Data Transfer Object for write operations on wishlists.
 *
 * <p>
 * This abstract DTO defines the common set of writable fields
 * shared between wishlist creation and update operations.
 * </p>
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@link WishlistCreateDTO} — used for creating a new wishlist (POST).</li>
 *   <li>{@link WishlistPatchDTO} — used for partially updating an existing wishlist (PATCH).</li>
 *   <li>{@link WishlistPutDTO} — used for fully updating an existing wishlist (PUT).</li>
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
 * <h3>PUT semantics</h3>
 * <p>
 * For PUT operations, this DTO represents a full update of writable fields.
 * All fields provided in the request are treated as the new state of the entity.
 * Depending on the API contract, missing or {@code null} fields may overwrite
 * existing values.
 * </p>
 *
 * <h3>Validation</h3>
 * <p>
 * Validation rules are applied using Bean Validation groups:
 * </p>
 * <ul>
 *   <li>{@code ValidationGroups.Create} — enforces required fields for creation.</li>
 *   <li>{@code ValidationGroups.Patch} — applies validation rules for partial updates.</li>
 *   <li>{@code ValidationGroups.Put} — applies validation rules for full updates.</li>
 * </ul>
 *
 * <p>
 * Constraints without explicit validation groups belong to the default group
 * and are applied to all operations unless explicitly overridden.
 * </p>
 *
 * <p>
 * This class does not include system-managed fields such as identifiers
 * or timestamps. Such fields must be handled by the service or persistence layer.
 * </p>
 *
 * @see WishlistCreateDTO
 * @see WishlistPatchDTO
 * @see WishlistPutDTO
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class WishlistWriteDTO {

    /**
     * Display title of the wishlist.
     *
     * <p>
     * On creation, this field is required and must not be blank.
     * On PATCH, if {@code null}, the title will not be changed.
     * </p>
     */
    @JsonProperty("title")
    @Length(min = 1, groups = ValidationGroups.Patch.class)
    @NotBlank(groups = ValidationGroups.Create.class)
    protected String title;

    /**
     * Optional description of the wishlist.
     *
     * <p>
     * On PATCH, if {@code null}, the description will not be changed.
     * On PUT, a {@code null} value may overwrite the existing description,
     * depending on the API contract.
     * </p>
     */
    @JsonProperty("description")
    protected String description;

    @JsonProperty("owner_id")
    @NotNull
    protected UUID ownerId;
}
