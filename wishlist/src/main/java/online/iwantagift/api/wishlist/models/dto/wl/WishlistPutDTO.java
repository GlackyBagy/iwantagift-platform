package online.iwantagift.api.wishlist.models.dto.wl;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishlistWriteDTO;

import java.util.UUID;

/**
 * Data Transfer Object for full replacement of an existing wishlist.
 *
 * <p>This DTO is used in PUT operations to update an existing wishlist
 * identified by its unique {@code id}.</p>
 *
 * <p>
 * All writable fields inherited from {@link WishlistWriteDTO} are treated
 * as part of a full update operation. Depending on the API contract,
 * omitted fields will be reset.
 * </p>
 *
 * <p>
 * The {@code id} field is mandatory and identifies the target wishlist
 * to be updated.
 * </p>
 *
 * <h3>Validation</h3>
 * <ul>
 *   <li>{@code ValidationGroups.Put} — enforces presence of {@code id}.</li>
 *   <li>{@code ValidationGroups.Create} — enforces required writable fields
 *       inherited from {@link WishlistWriteDTO} (via group inheritance).</li>
 * </ul>
 *
 * @see WishlistWriteDTO
 * @see ValidationGroups.Put
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WishlistPutDTO extends WishlistWriteDTO {

    /**
     * Unique identifier of the wishlist.
     */
    @JsonProperty("id")
    @NotNull(groups = ValidationGroups.Put.class)
    private UUID id;
}
