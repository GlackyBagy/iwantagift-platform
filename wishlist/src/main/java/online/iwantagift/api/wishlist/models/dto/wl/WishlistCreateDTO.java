package online.iwantagift.api.wishlist.models.dto.wl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishlistWriteDTO;

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
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCreateDTO extends WishlistWriteDTO {
}